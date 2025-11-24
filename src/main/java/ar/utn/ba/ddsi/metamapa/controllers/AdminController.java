package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.API.HechosApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private HechosApiClient hechosApi;


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
        // Esto busca el archivo en resources/templates/admin/inicioAdmin.html
        return "admin/inicioAdmin";
    }
}