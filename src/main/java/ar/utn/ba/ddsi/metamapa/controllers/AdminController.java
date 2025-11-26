package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.API.HechosApiClient;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private HechosApiClient hechosApi;

    private final ColeccionUiService coleccionService;


    @PostMapping("/importar-csv-upload")
    public ResponseEntity<?> subirCsv(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }
        try {
           // hechosApi.importarHechosCsv(file);
            return ResponseEntity.ok().body("Importación iniciada");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al procesar: " + e.getMessage());
        }
    }

    @GetMapping("/inicio")
    public String inicioAdmin() {
        return "admin/inicio";
    }

    @GetMapping("/colecciones")
    public String administrarColecciones(Model model, HttpSession session) {

        validarAdmin(session);

        model.addAttribute("colecciones", coleccionService.listarColecciones());
        return "admin/colecciones"; // tu template actual
    }

    private void validarAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("rol");
        if (!"ADMIN".equals(rol)) {
            throw new RuntimeException("403");
        }
    }
}