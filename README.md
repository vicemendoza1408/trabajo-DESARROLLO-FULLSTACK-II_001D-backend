# Fuma · Backend

Backend de la tienda online Fuma, para la Evaluación Parcial 3 de DSY1104 (Desarrollo Fullstack II). Es una API REST hecha con Spring Boot que guarda los datos en MySQL. El frontend (React) está en otro repositorio.

## Tecnologías
- Java 17 y Maven
- Spring Boot 3.3 (Web, Data JPA, Validation, Security)
- MySQL 8
- Swagger (springdoc-openapi 2.6)
- JWT (jjwt 0.12) para la autenticación y BCrypt para cifrar contraseñas

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
4. Crea los 3 usuarios de prueba (uno por rol) si no hay usuarios.

El servidor queda en `http://localhost:8080`.

## Cómo comprobar que funcionó
Con la consola de MySQL (`mysql -u root -p`):
```sql
USE fuma_db;
SHOW TABLES;
SELECT id, nombre, precio, descuento_porcentaje, id_categoria FROM producto;
```
Deben aparecer 5 tablas (`categoria`, `producto`, `usuario`, `boleta`, `detalle_boleta`) y 8 productos.
Además, `SELECT correo, rol, contrasena FROM usuario;` muestra 3 usuarios con la contraseña cifrada (empieza con `$2a$`).

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
├── dto/                   datos que entran (Solicitud...) y salen (...Respuesta) de la API
├── exception/             errores del negocio y su conversión a respuestas HTTP (ManejadorErrores)
├── util/                  cálculos de precios (Precios.java)
├── config/                datos iniciales, CORS, Swagger y reglas de seguridad (ConfiguracionSeguridad)
├── controller/            endpoints REST /api/v1
└── security/              JWT: creación y verificación de tokens, filtro por petición y respuestas 401/403
```

## Lógica de negocio (capa `service/`)
Los controladores solo reciben la petición y llaman a un servicio; las reglas viven en los servicios.

| Servicio | Qué hace |
|---|---|
| `CategoriaService` | Lista categorías y busca una por id |
| `ProductoService` | Lista el catálogo (todos, por categoría u ofertas); crea, edita y elimina productos con validación. No permite eliminar un producto que ya tiene ventas |
| `AuthService` | Inicio de sesión: busca al usuario por correo y compara la contraseña con el hash BCrypt |
| `BoletaService` | Crea la compra: valida los datos, simula el pago (rechaza tarjetas que empiezan con `0000` y nunca guarda el número), toma los precios de la base de datos, calcula subtotal, envío ($3.990 con despacho) y total, y guarda la boleta con sus detalles. Además, un cliente solo puede ver sus propias boletas |

Los cálculos de precios (`util/Precios.java`) y las validaciones son los mismos que ya tenía el frontend en `utils/precios.js` y `utils/validaciones.js`, ahora ejecutados en el servidor.

## API REST (versión v1)
Todas las rutas empiezan con `/api/v1`. La documentación interactiva (Swagger) está en
`http://localhost:8080/swagger-ui/index.html`, donde se puede probar cada endpoint.

| Método | Ruta | Quién puede usarlo | Descripción | Respuestas |
|---|---|---|---|---|
| POST | `/api/v1/auth/login` | Público | Inicia sesión y entrega el token JWT | 200, 400, 401 |
| POST | `/api/v1/auth/refresh` | Con sesión | Renueva el token (aún vigente) | 200, 401 |
| GET | `/api/v1/auth/me` | Con sesión | Datos del usuario dueño del token | 200, 401 |
| GET | `/api/v1/categorias` | Público | Lista las categorías | 200 |
| GET | `/api/v1/categorias/{id}` | Público | Detalle de una categoría | 200, 404 |
| GET | `/api/v1/productos` | Público | Lista productos (`?idCategoria=poleras` o `?ofertas=true`) | 200 |
| GET | `/api/v1/productos/{id}` | Público | Detalle de un producto | 200, 404 |
| POST | `/api/v1/productos` | ADMIN | Crea un producto | 201, 400, 401, 403 |
| PUT | `/api/v1/productos/{id}` | ADMIN | Actualiza un producto | 200, 400, 401, 403, 404 |
| DELETE | `/api/v1/productos/{id}` | ADMIN | Elimina un producto (no si ya tiene ventas) | 204, 400, 401, 403, 404 |
| POST | `/api/v1/boletas` | CLIENTE, ADMIN | Realiza una compra y genera la boleta | 201, 400, 401, 402, 403 |
| GET | `/api/v1/boletas` | ADMIN, VENDEDOR | Lista las boletas | 200, 401, 403 |
| GET | `/api/v1/boletas/{id}` | ADMIN, VENDEDOR; CLIENTE solo la suya | Detalle de una boleta | 200, 401, 403, 404 |

Errores: el backend responde con un JSON `{ "estado", "mensaje", "errores" }`. Los códigos usados son
400 (datos inválidos), 401 (sin sesión o credenciales incorrectas), 402 (pago rechazado),
403 (sin permiso para ese rol) y 404 (no existe).

## Seguridad (Spring Security + JWT)

**Flujo:**
1. El frontend envía `POST /api/v1/auth/login` con `correo` y `contrasena`.
2. `AuthService` busca al usuario y compara la contraseña con el hash BCrypt guardado.
3. Si coincide, `JwtServicio` crea un token JWT firmado (lleva el correo, el rol y la fecha de vencimiento; dura 30 minutos).
4. En cada petición protegida el frontend envía `Authorization: Bearer <token>`.
5. `FiltroJwt` verifica la firma y el vencimiento, y le informa a Spring Security quién es el usuario y su rol.
6. `ConfiguracionSeguridad` decide si ese rol puede usar el endpoint: si no hay sesión responde **401**; si hay sesión pero el rol no alcanza, **403**.
7. Antes de que el token venza, `POST /api/v1/auth/refresh` entrega uno nuevo. Con `GET /api/v1/auth/me` el frontend recupera la sesión al recargar la página.

**Permisos por rol:**

| Acción | Público | CLIENTE | VENDEDOR | ADMIN |
|---|:-:|:-:|:-:|:-:|
| Ver categorías y productos | sí | sí | sí | sí |
| Comprar | no | sí | no | sí |
| Ver sus propias boletas | no | sí | sí | sí |
| Ver la lista y el detalle de todas las boletas | no | no | sí | sí |
| Crear, editar y eliminar productos | no | no | no | sí |

**Usuarios de prueba** (se crean solos al arrancar; las contraseñas se guardan cifradas con BCrypt):

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | `admin@fuma.cl` | `admin123` |
| VENDEDOR | `vendedor@fuma.cl` | `vendedor123` |
| CLIENTE | `cliente@fuma.cl` | `cliente123` |

**Probar en Swagger:** ejecutar `POST /api/v1/auth/login`, copiar el `token` de la respuesta, presionar **Authorize**
(arriba a la derecha) y pegarlo. Los endpoints con candado ya usarán ese token.

La clave con la que se firman los tokens y su duración se configuran en `application.properties`
(`fuma.jwt.secreto`, que en un sistema real se define con la variable de entorno `JWT_SECRETO`, y `fuma.jwt.expiracion-minutos`).

**Prueba automática de los permisos:** con el backend corriendo, `bash scripts/probar-seguridad.sh` inicia sesión con cada rol,
hace peticiones permitidas y prohibidas, y compara el código HTTP obtenido con el esperado (crea 2 boletas de prueba).

## Pruebas automáticas
Se ejecutan con `mvn test` (o desde IntelliJ: clic derecho en `src/test/java` → Run Tests). Son 76 pruebas con JUnit 5, Mockito y MockMvc.
Las de integración usan **H2**, una base de datos en memoria (perfil `test`, archivo `src/test/resources/application-test.properties`),
así que no tocan la base MySQL real y no dejan datos de prueba.

| Clase | Tipo | Qué verifica |
|---|---|---|
| `PreciosTest` | Unitaria | Precio con descuento (con redondeo) y costo de envío |
| `JwtServicioTest` | Unitaria | Token válido, vencido, alterado o firmado con otra clave |
| `ProductoServiceTest` | Unitaria (Mockito) | Validaciones, producto inexistente y no eliminar productos con ventas |
| `BoletaServiceTest` | Unitaria (Mockito) | Precios tomados de la base, envío, pago rechazado, validaciones y boletas ajenas |
| `AuthApiTest` | Integración (MockMvc) | Login, `/auth/me`, renovación y tokens inválidos (401) |
| `ProductoApiTest` | Integración (MockMvc) | Consulta pública, CRUD del administrador y permisos por rol (401/403) |
| `BoletaApiTest` | Integración (MockMvc) | Compra completa, códigos 400/402/404 y permisos de lectura por rol |

