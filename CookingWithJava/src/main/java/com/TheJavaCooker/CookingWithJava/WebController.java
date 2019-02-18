package com.TheJavaCooker.CookingWithJava;

import com.TheJavaCooker.CookingWithJava.DataBase.*;
import net.bytebuddy.dynamic.loading.ClassInjector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
public class WebController {
    @Autowired
    private DatabaseManager database;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RecetaRepository recetaRepository;
    @Autowired
    private ImagendbRepository imagenRepository;

    private long usuarioActivoId = 2;

    //todo session para almacenar usuario

    @GetMapping(value={"/busqueda","/buscarReceta","buscar-receta"})
    public String buscar(Model model) {
        //todo recibir parametros del formularios y gestionarlos por POST
        return "busqueda";
    }

    @GetMapping(value={"/crearReceta","/crear-receta","/subir-receta","/subirReceta"})
    public String crearReceta(Model model) {
        return "crearReceta";
    }

    @GetMapping(value={"/inicio","/index","/"})
    public String index(Model model) {
        //todo recibir parametros del formularios y gestionarlos por POST
        return "index";
    }

    @GetMapping(value={"/login","/register"})
    public String login(Model model) {
        //todo recibir parametros del formularios y gestionarlos por POST
        //todo almacenar sesion actual
        return "login";
    }

    @RequestMapping(value={"/formulario-login"})
    public String formularioLogin(Model model, @RequestParam String nickLogin, @RequestParam String contrasenaLogin) {
        model.addAttribute("nickLogin", nickLogin);
        model.addAttribute("contrasenaLogin", contrasenaLogin);
        Usuario usuario = usuarioRepository.loginValido(nickLogin, contrasenaLogin);
        if(usuario==null) {
            //todo usuario o contraseña incorrectos
        } else {
            usuarioActivoId = usuario.getId();
            return "perfil-" + usuario.getId();
        }
        return "login";
    }

    @RequestMapping(value={"/formulario-registro"})
    public String formularioRegistro(Model model, @RequestParam String nickRegistro, @RequestParam String contrasenaRegistro, @RequestParam String correoRegistro, @RequestParam String nombreRegistro) {
        model.addAttribute("nickRegistro", nickRegistro);
        model.addAttribute("contrasenaRegistro", contrasenaRegistro);
        model.addAttribute("correoRegistro", correoRegistro);
        model.addAttribute("nombreRegistro", nombreRegistro);
        database.crearUsuario(nickRegistro, contrasenaRegistro, correoRegistro, nombreRegistro, DatabaseRandomData.getRandomUserImage());
        Usuario usuario = usuarioRepository.buscarPorNombreUsuario(nickRegistro);
        usuarioActivoId = usuario.getId();
        return "index";
    }

    @GetMapping(value={"/error"})
    public String error(Model model) {
        //todo pagina de error
        return "index";
    }

    @GetMapping(value={"/usuario-{usuarioId}","/perfil-{usuarioId}"})
    public String perfilId(Model model,@PathVariable long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        return returnPerfil(model,usuario);
    }

    @GetMapping(value={"/usuario","/perfil","/miusuario","/miperfil","/miUsuario","/miPerfil"})
    public String miPerfil(Model model) {
        Usuario usuario = usuarioRepository.findById(usuarioActivoId).orElse(null);
        return returnPerfil(model,usuario);
    }

    public String returnPerfil(Model model, Usuario usuario){
        if(usuario==null){
            //todo usuario no encontrado
            return "index";
        }
        model.addAttribute("num_recetas_subidas",usuario.getNumRecetasFavoritas());
        model.addAttribute("num_recetas_favoritas",usuario.getNumRecetasFavoritas());
        model.addAttribute("usuario",usuario);
        model.addAttribute("recetasCreadas",usuario.getRecetasCreadas());
        model.addAttribute("recetasFavoritas",usuario.getRecetasFavoritas());
        model.addAttribute("recetas",new ArrayList<Receta>());
        return "perfil";
    }

    @GetMapping(value={"/receta-{recetaId}","/receta-completa-{recetaId}","/recetaCompleta-{recetaId}"})
    public String mostrarReceta(Model model,@PathVariable long recetaId) {
        Receta receta = recetaRepository.findById(recetaId).orElse(null);
        if(receta==null){
            //todo receta no encontrada
            return "index";
        }
        model.addAttribute("receta",receta);
        model.addAttribute("fechaDeCreacion",receta.getStringFechaCreacion());
        model.addAttribute("duracionTotal",receta.getStringDuracionTotal());
        Usuario creadorReceta = receta.getCreadorDeLaReceta();
        model.addAttribute("creadorRecetaNombre",creadorReceta.getNombreUsuario());
        model.addAttribute("pasos",receta.getPasos());
        model.addAttribute("comentarios",receta.getComentarios());
        model.addAttribute("utensilios",receta.getUtensilios());
        model.addAttribute("ingredientes",receta.getIngredientes());
        return "receta-completa";
    }

    @GetMapping(value={"/recetas"})
    public String mostrarRecetas(Model model) {
        //todo misRecetas-favoritas muestra las recetas favoritas del usuario en la sesion actual
        //todo recibir por get parametros de busqueda ejecutar la busqueda
        //todo si no recibe parametros mostrar las ultimas recetas.
        //todo hacer un metodo auxiliar para gestionar las opciones
        model.addAttribute("recetas", database.getRecetaRepository().buscarTodas());
        return "recetas";
    }

    @GetMapping(value={"/recetas-favoritas-{usuarioId}"})
    public String mostrarRecetasFavoritas(Model model, @PathVariable long usuarioId) {
        //todo obtener id usuario actual
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        model.addAttribute("recetas", usuario.getRecetasFavoritas());
        return "recetas";
    }

    @GetMapping(value={"/recetas-creadas-{usuarioId}"})
    public String mostrarRecetasCreadas(Model model, @PathVariable long usuarioId) {
        //todo obtener id usuario actual
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        model.addAttribute("recetas", usuario.getRecetasCreadas());
        return "recetas";
    }

    @RequestMapping(value = {"/image/{imageId}","/image/{imageId}.jpg"})
    @ResponseBody
    public byte[] getImageUsuario(@PathVariable long imageId)  {
        Imagendb u = imagenRepository.findById(imageId).orElse(null);
        if(u != null){
            return u.getImagen();
        }
        else
            return DatabaseRandomData.getRandomRecipeImage();
    }
}
