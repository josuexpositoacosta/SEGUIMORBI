# Levantar la API en GitHub Codespaces

El repositorio trae un entorno de Codespaces (`.devcontainer/`) con Java 17 y MySQL 8. Al abrir el codespace,
la API arranca sola en el puerto 8080. Sirve para enseñar el sistema sin depender de la PC de desarrollo.

## 1. Secretos (una sola vez)

En GitHub: **Settings → Codespaces → Secrets → New secret** (en la cuenta del propietario del repositorio),
y en *Repository access* elegir el repositorio `SEGUIMORBI`:

| Secreto | Valor |
|---|---|
| `JWT_SECRET` | Resultado de `openssl rand -base64 32` |
| `ADMIN_PASSWORD` | Contraseña del usuario `admin` (mínimo 8 caracteres) |

La contraseña de MySQL no es secreta: la base solo es accesible dentro del codespace.

## 2. Crear el codespace

En el repositorio: **Code → Codespaces → Create codespace on `develop`** (o la rama que se quiera enseñar).
La primera vez tarda unos minutos (descarga imágenes y dependencias de Maven).

Para ver el arranque de la API, en la terminal del codespace:

```bash
tail -f /tmp/seguimorbi.log      # esperar a "Started SeguimorbiApplication"
```

Si no arrancó (por ejemplo, porque faltaban los secretos), se lanza a mano con `bash .devcontainer/arrancar.sh`.

## 3. Publicar el puerto

Pestaña **Ports** → puerto `8080` → clic derecho → **Port Visibility → Public**. La dirección queda así:

```
https://<nombre-del-codespace>-8080.app.github.dev
```

Comprobación (debe responder `401`, porque no lleva token):

```bash
curl -i https://<nombre-del-codespace>-8080.app.github.dev/api/v1/user/me
```

## 4. Tener en cuenta

- El codespace **se detiene tras 30 minutos sin uso** (configurable en Settings → Codespaces) y la dirección deja
  de responder. Al volver a abrirlo, la API arranca sola y la URL es la misma. Los datos de MySQL se conservan
  mientras no se borre el codespace.
- Las cuentas personales tienen un cupo gratuito mensual de horas de Codespaces: detenerlo al terminar
  (**Codespaces → Stop codespace**).
- CORS admite `http://localhost:4200` y `https://josuexpositoacosta.github.io` (frontend en GitHub Pages).
  Se cambia en `.devcontainer/docker-compose.yml` (`CORS_ALLOWED_ORIGINS`).
