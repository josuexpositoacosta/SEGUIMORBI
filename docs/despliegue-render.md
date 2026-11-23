# Desplegar la API gratis: Render + TiDB Cloud

Demo pública sin coste: la API en el plan gratuito de **Render** (Docker) y la base de datos en
**TiDB Cloud Starter** (compatible con MySQL; usa el mismo driver). El frontend va en GitHub Pages.

> Solo datos inventados: el plan gratuito no es un entorno para datos reales de pacientes.

## Limitaciones del plan gratuito

- Render **duerme la API tras 15 minutos sin peticiones**; la siguiente tarda en despertar (alrededor de un minuto,
  más el arranque de Spring Boot con una CPU muy pequeña). **Abrir la app unos 5 minutos antes de una demo.**
- 750 horas de instancia al mes por cuenta (mientras duerme no cuenta).
- No hay disco persistente: todo se guarda en TiDB.

## 1. Base de datos en TiDB Cloud

1. Crear cuenta en <https://tidbcloud.com> y un clúster **Starter** (gratuito). Región: la más cercana a la de Render
   (p. ej. `us-east-1` si Render está en Virginia/Ohio).
2. En el clúster: **Connect** → *Connect With*: **General** → *Operating System*: Linux. Pulsar
   **Generate Password** y copiar **host**, **port** (4000), **user** (`xxxx.root`) y la contraseña.
3. Crear la base de datos (en **SQL Editor** del clúster): `CREATE DATABASE seguimorbidb;`

La URL JDBC queda así (TLS obligatorio en TiDB Cloud; su certificado es público, no hace falta instalar nada):

```
jdbc:mysql://HOST:4000/seguimorbidb?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
```

## 2. API en Render

1. Crear cuenta en <https://render.com> (con GitHub) y dar acceso al repositorio `SEGUIMORBI`.
2. **New → Blueprint** → elegir el repositorio. Render lee `render.yaml` y crea el servicio `seguimorbi-api`
   (plan *Free*, rama `main`). Pedirá los valores secretos:

| Variable | Valor |
|---|---|
| `DB_URL` | la URL JDBC del paso 1 |
| `DB_USERNAME` | usuario de TiDB (`xxxx.root`) |
| `DB_PASSWORD` | contraseña de TiDB |
| `ADMIN_PASSWORD` | contraseña del usuario `admin` (mínimo 8 caracteres) |

`JWT_SECRET` lo genera Render; `CORS_ALLOWED_ORIGINS` ya incluye GitHub Pages y `localhost:4200`.

3. La primera compilación tarda varios minutos. La dirección queda como `https://seguimorbi-api.onrender.com`
   (Render añade un sufijo si el nombre está cogido).

Comprobación (debe responder `401`, porque no lleva token):

```bash
curl -i https://seguimorbi-api.onrender.com/api/v1/user/me
```

Registro de arranque: pestaña **Logs** del servicio (esperar a `Started SeguimorbiApplication`).

## 3. Datos de demostración

Para enseñar la app con contenido, `scripts/datos-demo.py` carga datos **ficticios** a través de la propia API
(2 áreas, 2 médicos y 1 epidemióloga, 12 pacientes y 10 casos con atención clínica, laboratorio y validaciones):

```bash
python3 scripts/datos-demo.py                        # contra https://seguimorbi-api.onrender.com
python3 scripts/datos-demo.py http://localhost:8080  # o contra otra API
```

Pide la contraseña de `admin` y no duplica nada si ya se cargó. Los usuarios de demo comparten la contraseña
`Demo.Seguimorbi2026`.

## 4. Actualizar

Cada push a `main` vuelve a desplegar la API (`autoDeploy`). Las tablas se crean y actualizan solas
(`spring.jpa.hibernate.ddl-auto=update`).

## Probar la imagen en local (opcional)

```bash
docker build -t seguimorbi-api .
docker run --rm -p 8080:8080 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... \
  -e JWT_SECRET="$(openssl rand -base64 32)" -e ADMIN_USERNAME=admin -e ADMIN_PASSWORD=... seguimorbi-api
```
