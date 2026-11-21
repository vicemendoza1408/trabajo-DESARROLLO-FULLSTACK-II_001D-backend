package cl.fuma.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del backend. @SpringBootApplication activa la configuración automática
// y busca los componentes (controladores, servicios, repositorios) dentro del paquete cl.fuma.tienda.
@SpringBootApplication
public class FumaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FumaApplication.class, args);
    }
}
