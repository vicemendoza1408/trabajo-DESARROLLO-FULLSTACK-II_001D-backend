# Fuma · Backend

Backend de la tienda online Fuma, para la Evaluación Parcial 3 de DSY1104 (Desarrollo Fullstack II). Es una API REST hecha con Spring Boot que guarda los datos en MySQL. El frontend (React) está en otro repositorio.

## Tecnologías
- Java 17 y Maven
- Spring Boot 3.3 (Web, Data JPA, Validation)
- MySQL 8
- Se agregan en fases siguientes: Swagger (springdoc), Spring Security y JWT

## Requisitos
- Java 17 o superior
- Maven 3.9 o superior
- MySQL corriendo en `localhost:3306`

## Cómo ejecutarlo
```bash
# Opcional: usuario y clave de MySQL (por defecto: usuario root, sin clave)
export DB_USUARIO=root
export DB_CLAVE=tu_clave

mvn spring-boot:run
```

Al arrancar, el backend:
1. Crea la base `fuma_db` si no existe.
2. Crea las tablas a partir de las entidades (`spring.jpa.hibernate.ddl-auto=update`).
3. Carga las 4 categorías y los 8 productos iniciales si la base está vacía.

El servidor queda en `http://localhost:8080`.

## Cómo comprobar que funcionó
Con la consola de MySQL (`mysql -u root -p`):
```sql
USE fuma_db;
SHOW TABLES;
SELECT id, nombre, precio, descuento_porcentaje, id_categoria FROM producto;
```
Deben aparecer 5 tablas (`categoria`, `producto`, `usuario`, `boleta`, `detalle_boleta`) y 8 productos.

## Modelo de datos
Las 5 tablas del enunciado (Figura 1):

```
USUARIO ──< BOLETA ──< DETALLE_BOLETA >── PRODUCTO >── CATEGORIA
```

| Tabla | Qué guarda | Relaciones |
|---|---|---|
| `categoria` | Polerones, Poleras, Pantalones, Accesorios | Una categoría tiene muchos productos |
| `producto` | Nombre, descripción, precio, descuento, imagen | Pertenece a una categoría |
| `usuario` | Nombre, correo, contraseña cifrada, teléfono, dirección, rol | Un usuario tiene muchas boletas |
| `boleta` | Fecha, datos de contacto, entrega, subtotal, envío y total | Pertenece a un usuario; tiene muchos detalles |
| `detalle_boleta` | Producto, cantidad y precio unitario al comprar | Pertenece a una boleta y a un producto |

Roles del sistema: `ADMIN` (acceso total), `VENDEDOR` (solo ve productos y órdenes) y `CLIENTE` (solo la tienda).

## Estructura del código
```
src/main/java/cl/fuma/tienda/
├── FumaApplication.java   arranque de la aplicación
├── model/                 entidades: una clase por tabla (y el enum Rol)
├── repository/            acceso a la base de datos (un repositorio por entidad)
├── service/               lógica de negocio: reglas, validaciones y cálculos
├── dto/                   datos que envía el frontend (SolicitudProducto, SolicitudBoleta)
├── exception/             errores del negocio (no encontrado, datos inválidos, pago rechazado)
├── util/                  cálculos de precios (Precios.java)
├── config/                carga de datos iniciales
├── controller/            endpoints REST /api/v1 (Fase 3)
└── security/              JWT y Spring Security (Fase 4)
```

## Lógica de negocio (capa `service/`)
Los controladores solo reciben la petición y llaman a un servicio; las reglas viven en los servicios.

| Servicio | Qué hace |
|---|---|
| `CategoriaService` | Lista categorías y busca una por id |
| `ProductoService` | Lista el catálogo (todos, por categoría u ofertas); crea, edita y elimina productos con validación. No permite eliminar un producto que ya tiene ventas |
| `BoletaService` | Crea la compra: valida los datos, simula el pago (rechaza tarjetas que empiezan con `0000` y nunca guarda el número), toma los precios de la base de datos, calcula subtotal, envío ($3.990 con despacho) y total, y guarda la boleta con sus detalles |

Los cálculos de precios (`util/Precios.java`) y las validaciones son los mismos que ya tenía el frontend en `utils/precios.js` y `utils/validaciones.js`, ahora ejecutados en el servidor.
