#!/usr/bin/env python3
"""
Carga datos de DEMOSTRACIÓN (ficticios) en una API de SEGUIMORBI usando sus propios endpoints,
así pasan por las mismas validaciones y reglas de la OPS que la app.

Uso:
    python3 scripts/datos-demo.py [URL_API]          # por defecto https://seguimorbi-api.onrender.com

Pide la contraseña del usuario admin (no se guarda). Crea:
  - 2 áreas de salud, 3 usuarios (2 médicos y 1 epidemióloga) con la contraseña DEMO_PASSWORD,
  - 12 pacientes y 10 casos con atención clínica, resultados de laboratorio y validaciones.
Si ya se cargó antes (las áreas existen), se detiene sin duplicar nada.

Todos los nombres y documentos son inventados. No usar con datos reales.
"""
import getpass
import json
import sys
import urllib.error
import urllib.request
from datetime import date, timedelta

API = (sys.argv[1] if len(sys.argv) > 1 else "https://seguimorbi-api.onrender.com").rstrip("/")
DEMO_PASSWORD = "Demo.Seguimorbi2026"
HOY = date.today()


def dia(dias_atras: int) -> str:
    return (HOY - timedelta(days=dias_atras)).isoformat()


def futuro(dias: int) -> str:
    return (HOY + timedelta(days=dias)).isoformat()


class ErrorApi(Exception):
    def __init__(self, status, cuerpo):
        super().__init__(f"HTTP {status}: {cuerpo}")
        self.status = status


def peticion(metodo, ruta, datos=None, token=None):
    req = urllib.request.Request(
        API + ruta,
        method=metodo,
        data=json.dumps(datos).encode() if datos is not None else None,
        headers={"Content-Type": "application/json", **({"Authorization": f"Bearer {token}"} if token else {})},
    )
    try:
        # La API gratuita puede estar dormida: la primera petición tarda hasta ~2 minutos.
        with urllib.request.urlopen(req, timeout=180) as r:
            cuerpo = r.read().decode()
            return json.loads(cuerpo) if cuerpo else None
    except urllib.error.HTTPError as e:
        raise ErrorApi(e.code, e.read().decode()[:300]) from None


def login(usuario, password):
    return peticion("POST", "/auth/login", {"username": usuario, "password": password})["token"]


def paso(texto):
    print(f"  · {texto}", flush=True)


# ---------------------------------------------------------------- datos ficticios

AREAS = [
    {"codigo": "DEMO-CENTRO", "nombre": "Área de Salud Centro", "provincia": "Provincia Demo",
     "municipio": "Centro", "direccion": "Calle Principal 100", "telefono": "555-0100"},
    {"codigo": "DEMO-NORTE", "nombre": "Área de Salud Norte", "provincia": "Provincia Demo",
     "municipio": "Norte", "direccion": "Avenida del Norte 25", "telefono": "555-0200"},
]

USUARIOS = [
    {"username": "medico.centro", "firstname": "Laura", "lastname": "Méndez Ruiz", "role": "MEDICO", "area": 0,
     "email": "medico.centro@demo.seguimorbi"},
    {"username": "medico.norte", "firstname": "Carlos", "lastname": "Ortega Lima", "role": "MEDICO", "area": 1,
     "email": "medico.norte@demo.seguimorbi"},
    {"username": "epidemiologa", "firstname": "Ana", "lastname": "Castillo Vera", "role": "EPIDEMIOLOGO", "area": None,
     "email": "epidemiologa@demo.seguimorbi"},
]

# (nombres, apellidos, fecha de nacimiento, sexo)
PACIENTES = [
    [("María", "González Pérez", "1988-03-14", "F"), ("José", "Rodríguez Díaz", "1975-07-02", "M"),
     ("Yanet", "Hernández Cruz", "1996-11-21", "F"), ("Pedro", "Martínez Sosa", "1960-01-30", "M"),
     ("Lucía", "Fernández Mora", "2010-05-09", "F"), ("Andrés", "López Vidal", "1983-09-17", "M")],
    [("Rosa", "Pérez Molina", "1992-02-25", "F"), ("Miguel", "Sánchez Rey", "1979-12-03", "M"),
     ("Elena", "Ramírez Soto", "1999-06-11", "F"), ("Jorge", "Torres Gil", "1968-08-27", "M"),
     ("Carmen", "Flores Ibáñez", "1985-04-19", "F"), ("Daniel", "Navarro Paz", "2004-10-05", "M")],
]


def documento(fecha_nac: str, i: int) -> str:
    a, m, d = fecha_nac.split("-")
    return f"{a[2:]}{m}{d}{90000 + i:05d}"


def datos_caso(paciente_id, dias_atras, **extra):
    base = {
        "pacienteId": paciente_id,
        "fechaInicioSintomas": dia(dias_atras + 2),
        "fechaNotificacion": dia(dias_atras),
        "fiebre": True,
        "cefaleaIntensa": True,
        "manifestaciones": ["MIALGIA_ARTRALGIA", "ESCALOFRIOS"],
        "recaida": False,
        "embarazada": False,
        "semanasGestacion": None,
        "areaTransmisionOViaje": True,
        "detalleAreaTransmision": "Zona con transmisión activa notificada",
        "nexoCasoConfirmado": False,
    }
    base.update(extra)
    return base


def main():
    print(f"API: {API}")
    password_admin = getpass.getpass("Contraseña del usuario admin: ")

    print("Iniciando sesión como admin (si la API dormía, puede tardar 1–2 minutos)…", flush=True)
    t_admin = login("admin", password_admin)

    existentes = {a["codigo"] for a in peticion("GET", "/api/v1/areas-salud", token=t_admin)}
    if any(a["codigo"] in existentes for a in AREAS):
        print("Los datos de demostración ya están cargados (existen las áreas DEMO-*). No se hace nada.")
        return

    print("Áreas de salud y usuarios")
    areas = []
    for a in AREAS:
        areas.append(peticion("POST", "/api/v1/areas-salud", a, t_admin)["id"])
        paso(a["nombre"])
    for u in USUARIOS:
        peticion("POST", "/api/v1/admin/usuarios", {
            "username": u["username"], "email": u["email"], "password": DEMO_PASSWORD,
            "firstname": u["firstname"], "lastname": u["lastname"], "country": "Demo",
            "role": u["role"], "areaSaludId": areas[u["area"]] if u["area"] is not None else None,
        }, t_admin)
        paso(f"{u['username']} ({u['role']})")

    t_med = [login("medico.centro", DEMO_PASSWORD), login("medico.norte", DEMO_PASSWORD)]
    t_epi = login("epidemiologa", DEMO_PASSWORD)

    print("Pacientes")
    pac = [[], []]
    n = 0
    for i_area, lista in enumerate(PACIENTES):
        for nombres, apellidos, nac, sexo in lista:
            n += 1
            p = peticion("POST", "/api/v1/pacientes", {
                "tipoDocumento": "CI", "numeroDocumento": documento(nac, n), "nombres": nombres,
                "apellidos": apellidos, "fechaNacimiento": nac, "sexo": sexo,
                "direccion": f"Calle {n * 7}, nº {n + 10}", "telefono": f"555-{1000 + n}",
                "areaSaludId": areas[i_area],
            }, t_med[i_area])
            pac[i_area].append(p["id"])
        paso(f"{len(lista)} pacientes en {AREAS[i_area]['nombre']}")

    def caso(i_area, i_pac, dias_atras, **extra):
        return peticion("POST", "/api/v1/casos", datos_caso(pac[i_area][i_pac], dias_atras, **extra), t_med[i_area])["id"]

    def post(i_area, caso_id, que, datos):
        return peticion("POST", f"/api/v1/casos/{caso_id}/{que}", datos, t_med[i_area])

    def patch(token, caso_id, que, datos):
        return peticion("PATCH", f"/api/v1/casos/{caso_id}/{que}", datos, token)

    def pcr(i_area, caso_id, agente, resultado, dias_atras):
        post(i_area, caso_id, "resultados", {
            "agente": agente, "tipoPrueba": "RT_PCR", "tipoMuestra": "SUERO", "resultado": resultado,
            "fechaToma": dia(dias_atras), "fechaResultado": dia(max(dias_atras - 2, 0)),
        })

    def evaluacion(i_area, caso_id, temp, fc, signos=(), alarma=(), obs=None):
        post(i_area, caso_id, "evaluaciones", {
            "temperatura": temp, "frecuenciaCardiaca": fc, "frecuenciaRespiratoria": 18,
            "tensionSistolica": 118, "tensionDiastolica": 76, "saturacionO2": 97,
            "signosClinicos": list(signos), "signosAlarmaDengue": list(alarma), "observaciones": obs,
        })

    def clasificar(i_area, caso_id, clasificacion, motivo):
        """Cambia la clasificación; si la API ya la asignó sola (reglas OPS), lo deja como está."""
        try:
            patch(t_med[i_area], caso_id, "clasificacion", {"clasificacion": clasificacion, "motivo": motivo})
        except ErrorApi as e:
            if e.status != 400 or "ya tiene la clasificación" not in str(e):
                raise

    def confirmar(i_area, caso_id):
        clasificar(i_area, caso_id, "CONFIRMADO", "RT-PCR positiva a virus Oropouche")

    print("Casos y atención clínica")

    # 1. Confirmado, cerrado y recuperado (hace 5 semanas)
    c = caso(0, 0, 36, manifestaciones=["MIALGIA_ARTRALGIA", "ESCALOFRIOS", "DOLOR_RETROOCULAR"])
    pcr(0, c, "OROPOUCHE", "POSITIVO", 35)
    confirmar(0, c)
    evaluacion(0, c, 38.9, 104, ["POSTRACION", "LUMBALGIA"], obs="Cefalea intensa y artralgias.")
    post(0, c, "dictamenes", {"diagnosticoPrincipal": "Fiebre de Oropouche",
                              "diagnosticosDiferenciales": "Dengue", "conclusion": "Caso confirmado por RT-PCR, sin signos de alarma."})
    post(0, c, "planes", {"modalidad": "AMBULATORIO", "indicaciones": "Reposo, hidratación oral y paracetamol.", "proximoControl": None})
    post(0, c, "seguimientos", {"evolucion": "MEJORIA", "observaciones": "Afebril, mejoría de las artralgias.", "proximoControl": None})
    patch(t_med[0], c, "cierre", {"desenlace": "RECUPERADO", "fechaCierre": dia(20), "observaciones": "Alta sin secuelas."})
    patch(t_epi, c, "clasificacion/validacion", {"motivo": "Validado con resultado de laboratorio."})
    paso("Caso 1: confirmado, cerrado (recuperado) y validado")

    # 2. Confirmado, cerrado y recuperado (hace 4 semanas, área Norte)
    c = caso(1, 0, 29)
    pcr(1, c, "OROPOUCHE", "POSITIVO", 28)
    confirmar(1, c)
    evaluacion(1, c, 38.4, 98, ["LUMBALGIA"])
    post(1, c, "planes", {"modalidad": "AMBULATORIO", "indicaciones": "Tratamiento sintomático.", "proximoControl": None})
    post(1, c, "seguimientos", {"evolucion": "MEJORIA", "observaciones": "Evolución favorable.", "proximoControl": None})
    patch(t_med[1], c, "cierre", {"desenlace": "RECUPERADO", "fechaCierre": dia(15), "observaciones": None})
    patch(t_epi, c, "clasificacion/validacion", {"motivo": None})
    paso("Caso 2: confirmado, cerrado (recuperado) y validado")

    # 3. Confirmado con manifestación neurológica, ingresado (hace 3 semanas)
    c = caso(0, 3, 22, manifestaciones=["MIALGIA_ARTRALGIA", "FOTOFOBIA", "NEUROLOGICA"])
    pcr(0, c, "OROPOUCHE", "POSITIVO", 21)
    confirmar(0, c)
    evaluacion(0, c, 39.4, 112, ["LETARGIA", "RIGIDEZ_NUCA"], obs="Signos meníngeos; valorar punción lumbar.")
    post(0, c, "dictamenes", {"diagnosticoPrincipal": "Fiebre de Oropouche con afectación neurológica",
                              "diagnosticosDiferenciales": "Meningitis bacteriana", "conclusion": "Requiere ingreso y vigilancia neurológica."})
    post(0, c, "planes", {"modalidad": "INGRESO_HOSPITALARIO", "indicaciones": "Ingreso, vigilancia neurológica cada 4 horas.",
                          "proximoControl": futuro(1)})
    post(0, c, "seguimientos", {"evolucion": "ESTABLE", "observaciones": "Sin nuevos déficits.", "proximoControl": futuro(2)})
    patch(t_epi, c, "clasificacion/validacion", {"motivo": "Caso con afectación neurológica: notificar."})
    paso("Caso 3: confirmado, ingresado, con alertas neurológicas")

    # 4. Confirmado reciente, embarazada (hace 2 semanas, área Norte)
    c = caso(1, 2, 13, embarazada=True, semanasGestacion=20)
    pcr(1, c, "OROPOUCHE", "POSITIVO", 12)
    confirmar(1, c)
    evaluacion(1, c, 38.2, 96, obs="Gestante de 20 semanas; control obstétrico.")
    post(1, c, "planes", {"modalidad": "INGRESO_HOSPITALARIO", "indicaciones": "Ingreso por gestación; seguimiento obstétrico.",
                          "proximoControl": futuro(3)})
    paso("Caso 4: confirmado en gestante (alerta), pendiente de validar")

    # 5. Probable con nexo epidemiológico (hace 12 días)
    c = caso(0, 1, 12, nexoCasoConfirmado=True)
    clasificar(0, c, "PROBABLE", "Nexo con caso confirmado del área.")
    evaluacion(0, c, 38.6, 100, ["POSTRACION"])
    post(0, c, "planes", {"modalidad": "AMBULATORIO", "indicaciones": "Sintomáticos y toma de muestra.", "proximoControl": futuro(2)})
    paso("Caso 5: probable, pendiente de validar")

    # 6. Probable (hace 9 días, área Norte)
    c = caso(1, 1, 9, nexoCasoConfirmado=True, manifestaciones=["MIALGIA_ARTRALGIA", "GASTROINTESTINAL"])
    clasificar(1, c, "PROBABLE", "Conviviente de caso confirmado.")
    evaluacion(1, c, 38.0, 92, ["VOMITOS_PERSISTENTES"], ["VOMITOS_PERSISTENTES"])
    post(1, c, "planes", {"modalidad": "AMBULATORIO", "indicaciones": "Hidratación y vigilar signos de alarma.", "proximoControl": futuro(1)})
    paso("Caso 6: probable con signo de alarma de dengue")

    # 7. Descartado (RT-PCR negativa a Oropouche y dengue)
    c = caso(0, 2, 16)
    pcr(0, c, "OROPOUCHE", "NEGATIVO", 15)
    pcr(0, c, "DENGUE", "NEGATIVO", 15)
    clasificar(0, c, "DESCARTADO", "RT-PCR negativa a Oropouche y dengue.")
    patch(t_epi, c, "clasificacion/validacion", {"motivo": None})
    paso("Caso 7: descartado y validado")

    # 8–10. Sospechosos recientes (última semana)
    # Sin zona de transmisión ni nexo: quedan como sospechosos a la espera de laboratorio.
    sin_nexo = {"areaTransmisionOViaje": False, "detalleAreaTransmision": None}
    c = caso(0, 4, 5, manifestaciones=["MIALGIA_ARTRALGIA", "ESCALOFRIOS", "MAREOS"], **sin_nexo)
    evaluacion(0, c, 38.7, 102)
    post(0, c, "planes", {"modalidad": "AMBULATORIO", "indicaciones": "Sintomáticos; muestra para RT-PCR.", "proximoControl": futuro(1)})
    c = caso(1, 3, 3, **sin_nexo)
    evaluacion(1, c, 39.0, 106, ["LUMBALGIA"])
    caso(1, 4, 1, manifestaciones=["MIALGIA_ARTRALGIA", "DOLOR_RETROOCULAR"], **sin_nexo)
    paso("Casos 8–10: sospechosos de la última semana")

    print("\nListo. Usuarios de demostración (contraseña para los tres: %s):" % DEMO_PASSWORD)
    for u in USUARIOS:
        print(f"  - {u['username']:<14} {u['role']}")


if __name__ == "__main__":
    try:
        main()
    except ErrorApi as e:
        print(f"\nLa API respondió con un error: {e}", file=sys.stderr)
        sys.exit(1)
