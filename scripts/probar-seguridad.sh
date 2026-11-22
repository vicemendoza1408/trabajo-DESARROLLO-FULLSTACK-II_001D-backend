#!/bin/bash
# Prueba rápida de la seguridad del backend (Fase 4).
# Con el backend corriendo, ejecuta peticiones como cada rol y compara el código HTTP obtenido con el esperado.
#
#   Uso:  bash scripts/probar-seguridad.sh
#
# Nota: crea 2 boletas de prueba (una del administrador y una del cliente) y un producto que borra al final.

URL="http://localhost:8080/api/v1"
FALLAS=0

# Inicia sesión y devuelve solo el token (el texto entre comillas después de "token":).
login() {
  curl -s -X POST "$URL/auth/login" -H "Content-Type: application/json" \
    -d "{\"correo\":\"$1\",\"contrasena\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'
}

# prueba "descripción" CÓDIGO_ESPERADO MÉTODO /ruta [token] [cuerpo JSON]
prueba() {
  local descripcion="$1" esperado="$2" metodo="$3" ruta="$4" token="$5" cuerpo="$6"
  local args=(-s -o /dev/null -w "%{http_code}" -X "$metodo" "$URL$ruta" -H "Content-Type: application/json")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$cuerpo" ] && args+=(-d "$cuerpo")
  local real
  real=$(curl "${args[@]}")
  if [ "$real" = "$esperado" ]; then
    echo "  OK     $descripcion  (HTTP $real)"
  else
    echo "  FALLA  $descripcion  (esperado $esperado, obtuvo $real)"
    FALLAS=$((FALLAS + 1))
  fi
}

PRODUCTO='{"nombre":"Producto de prueba","descripcion":"Solo para probar","precio":10000,"descuentoPorcentaje":0,"idCategoria":"accesorios"}'
COMPRA='{"nombre":"Cliente Demo","correo":"cliente@fuma.cl","telefono":"912345678","opcionEntrega":"retiro","numeroTarjeta":"4111111111111111","items":[{"idProducto":1,"cantidad":1}]}'

echo "Iniciando sesión con los 3 usuarios de prueba..."
TOKEN_ADMIN=$(login admin@fuma.cl admin123)
TOKEN_VENDEDOR=$(login vendedor@fuma.cl vendedor123)
TOKEN_CLIENTE=$(login cliente@fuma.cl cliente123)
# Un token JWT siempre empieza con "eyJ". Si no, el login falló.
if [[ "$TOKEN_ADMIN" != eyJ* ]] || [[ "$TOKEN_VENDEDOR" != eyJ* ]] || [[ "$TOKEN_CLIENTE" != eyJ* ]]; then
  echo "No se pudo iniciar sesión. ¿Está corriendo el backend en http://localhost:8080?"
  exit 1
fi

echo; echo "Login"
prueba "Contraseña incorrecta" 401 POST /auth/login "" '{"correo":"admin@fuma.cl","contrasena":"mala"}'
prueba "Faltan datos" 400 POST /auth/login "" '{"correo":""}'

echo; echo "Sin sesión (público)"
prueba "Ver productos" 200 GET /productos
prueba "Ver categorías" 200 GET /categorias
prueba "Crear producto sin token" 401 POST /productos "" "$PRODUCTO"
prueba "Ver boletas sin token" 401 GET /boletas
prueba "Comprar sin token" 401 POST /boletas "" "$COMPRA"
prueba "Token falso" 401 GET /auth/me "token.falso.123"

echo; echo "Sesión (/me y renovación)"
prueba "/auth/me con token válido" 200 GET /auth/me "$TOKEN_CLIENTE"
prueba "Renovar token" 200 POST /auth/refresh "$TOKEN_CLIENTE"

echo; echo "CLIENTE"
prueba "Crear producto" 403 POST /productos "$TOKEN_CLIENTE" "$PRODUCTO"
prueba "Eliminar producto" 403 DELETE /productos/1 "$TOKEN_CLIENTE"
prueba "Ver lista de boletas" 403 GET /boletas "$TOKEN_CLIENTE"
# La compra se hace con curl directo para leer el id de la boleta creada (se usa más abajo).
RESPUESTA=$(curl -s -w " %{http_code}" -X POST "$URL/boletas" -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_CLIENTE" -d "$COMPRA")
ID_CLIENTE=$(echo "$RESPUESTA" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [[ "$RESPUESTA" == *" 201" ]]; then
  echo "  OK     Comprar  (HTTP 201, boleta $ID_CLIENTE)"
else
  echo "  FALLA  Comprar  (respuesta: $RESPUESTA)"; FALLAS=$((FALLAS + 1))
fi
prueba "Comprar con tarjeta rechazada" 402 POST /boletas "$TOKEN_CLIENTE" "${COMPRA/4111111111111111/0000111122223333}"

echo; echo "VENDEDOR"
prueba "Ver lista de boletas" 200 GET /boletas "$TOKEN_VENDEDOR"
prueba "Ver productos" 200 GET /productos "$TOKEN_VENDEDOR"
prueba "Crear producto" 403 POST /productos "$TOKEN_VENDEDOR" "$PRODUCTO"
prueba "Comprar" 403 POST /boletas "$TOKEN_VENDEDOR" "$COMPRA"

echo; echo "ADMIN"
prueba "Ver lista de boletas" 200 GET /boletas "$TOKEN_ADMIN"
RESPUESTA=$(curl -s -w " %{http_code}" -X POST "$URL/boletas" -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_ADMIN" -d "$COMPRA")
ID_ADMIN=$(echo "$RESPUESTA" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [[ "$RESPUESTA" == *" 201" ]]; then
  echo "  OK     Comprar  (HTTP 201, boleta $ID_ADMIN)"
else
  echo "  FALLA  Comprar  (respuesta: $RESPUESTA)"; FALLAS=$((FALLAS + 1))
fi
# Crea un producto, lo edita y lo elimina (leyendo su id de la respuesta).
RESPUESTA=$(curl -s -X POST "$URL/productos" -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_ADMIN" -d "$PRODUCTO")
ID=$(echo "$RESPUESTA" | sed -E 's/.*"id":([0-9]+).*/\1/')
if [[ "$ID" =~ ^[0-9]+$ ]]; then
  echo "  OK     Crear producto  (se creó el producto $ID)"
  prueba "Editar producto" 200 PUT "/productos/$ID" "$TOKEN_ADMIN" "$PRODUCTO"
  prueba "Eliminar producto" 204 DELETE "/productos/$ID" "$TOKEN_ADMIN"
else
  echo "  FALLA  Crear producto  (respuesta: $RESPUESTA)"
  FALLAS=$((FALLAS + 1))
fi

echo; echo "Boletas de otros usuarios"
# ID_ADMIN e ID_CLIENTE son las boletas creadas más arriba por el administrador y por el cliente.
prueba "Admin ve el detalle de una boleta" 200 GET "/boletas/$ID_ADMIN" "$TOKEN_ADMIN"
prueba "Vendedor ve el detalle de una boleta" 200 GET "/boletas/$ID_ADMIN" "$TOKEN_VENDEDOR"
prueba "Cliente ve su propia boleta" 200 GET "/boletas/$ID_CLIENTE" "$TOKEN_CLIENTE"
prueba "Cliente intenta ver la boleta de otro" 403 GET "/boletas/$ID_ADMIN" "$TOKEN_CLIENTE"
prueba "Boleta que no existe" 404 GET /boletas/999999 "$TOKEN_ADMIN"

echo
if [ "$FALLAS" -eq 0 ]; then
  echo "Todas las pruebas pasaron."
else
  echo "$FALLAS prueba(s) fallaron."
  exit 1
fi
