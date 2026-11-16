# API Techlab

API REST de un E-commerce desarrollada con Spring Boot y MySQL: productos, categorías, pedidos con control de stock e historial por usuario.

## Tecnologías

- Java 17
- Spring Boot 4.1.2 (Spring Web MVC)
- Spring Data JPA / Hibernate
- MySQL
- Maven (incluye Maven Wrapper)

## Requisitos previos

- JDK 17 o superior
- MySQL en ejecución en `localhost:3306` (usuario `root` sin contraseña por defecto; se cambia en `application.properties`)

## Estructura

```
src/main/java/com/techlab/api_techlab/
├── controller/   ProductoController, CategoriaController, PedidoController
├── service/      ProductoService, CategoriaService, PedidoService (lógica de negocio)
├── repository/   Producto, Categoria, Usuario y Pedido (Spring Data JPA)
├── model/        Producto, Categoria, Usuario, Pedido, LineaPedido, EstadoPedido
├── dto/          Cuerpos de entrada y salida de la API (ProductoRequest, PedidoRequest, PedidoResponse...)
├── exception/    Excepciones propias + GlobalExceptionHandler
├── util/         Validador
└── config/       CorsConfig (permite consumir la API desde el frontend)
```

Arquitectura en capas: **controller → service → repository → base de datos**.

## Base de datos

Al iniciar, Hibernate crea o actualiza las tablas (`ddl-auto=update`) y `data.sql` carga las categorías (Ropa, Calzado, Accesorios, Alimentos) y un usuario de prueba (`id 1`, `demo@techlab.com`). La base `tienda` se crea sola si no existe.

## Cómo ejecutar

```bash
./mvnw spring-boot:run      # Linux / macOS
mvnw.cmd spring-boot:run    # Windows
```

La API queda en `http://localhost:8080`. Tests: `./mvnw test` (el test `contextLoads` necesita MySQL activo; `PedidoServiceTest` no).

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/categorias` | Categorías para el desplegable | 200 |
| GET | `/api/productos` | Lista de productos | 200 |
| GET | `/api/productos/{id}` | Un producto | 200, 404 |
| GET | `/api/productos/categoria/{nombre}` | Productos de una categoría | 200 |
| POST | `/api/productos` | Crea un producto | 201, 400, 404 (categoría) |
| PUT | `/api/productos/{id}` | Actualiza un producto | 200, 400, 404 |
| DELETE | `/api/productos/{id}` | Elimina un producto | 204, 404, 409 (está en un pedido) |
| POST | `/api/pedidos` | Crea un pedido y descuenta stock | 201, 400 (stock/datos), 404 |
| GET | `/api/usuarios/{id}/pedidos` | Historial de pedidos del usuario | 200, 404 |

Cuerpo de `POST /api/productos`:

```json
{
  "nombre": "Café Premium",
  "descripcion": "Café colombiano tostado",
  "precio": 12.50,
  "categoriaId": 3,
  "imagenUrl": "http://ejemplo.com/cafe.jpg",
  "stock": 100
}
```

Cuerpo de `POST /api/pedidos`:

```json
{
  "usuarioId": 1,
  "lineas": [
    { "productoId": 1, "cantidad": 2 },
    { "productoId": 3, "cantidad": 1 }
  ]
}
```

Si algún producto no tiene stock suficiente se responde `400 Bad Request` con un mensaje claro y no se descuenta nada (todo el pedido es una transacción). Si hay stock, se descuenta, se guarda el precio vigente en cada línea y el pedido queda en estado `PENDIENTE`.

## Pruebas manuales

`pruebas.http` tiene un ejemplo de cada endpoint para la extensión REST Client de VS Code.

## Autor

Miguel Ángel Nefle
