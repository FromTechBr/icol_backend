#!/usr/bin/env python3
"""Carrega as variáveis de .env e inicia o backend Spring Boot."""

import os
import subprocess
import sys
from pathlib import Path


def load_env_file(env_path: Path) -> bool:
    if not env_path.is_file():
        return False

    with env_path.open(encoding="utf-8") as env_file:
        for line_number, raw_line in enumerate(env_file, start=1):
            line = raw_line.strip()
            if not line or line.startswith("#"):
                continue

            if line.startswith("export "):
                line = line[len("export ") :].lstrip()

            key, separator, value = line.partition("=")
            key = key.strip()
            if not separator or not key:
                print(f"Formato inválido no .env, linha {line_number}.", file=sys.stderr)
                return False

            value = value.strip()
            if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
                value = value[1:-1]
            elif " #" in value:
                value = value.split(" #", maxsplit=1)[0].rstrip()

            os.environ[key] = value

    return True


def main() -> int:
    backend_dir = Path(__file__).resolve().parent
    use_testcontainers = "--testcontainers" in sys.argv or "-t" in sys.argv
    has_env = load_env_file(backend_dir / ".env")

    if not has_env and not use_testcontainers:
        print("Arquivo .env não encontrado!")
        return 1

    if has_env:
        print("Variáveis carregadas do .env:")
        if not use_testcontainers:
            print("   DB_URL: " + os.environ.get("DB_URL", "jdbc:postgresql://localhost:5432/icol_db"))
            print("   DB_USERNAME: " + os.environ.get("DB_USERNAME", "postgres"))
        print("   JWT_SECRET: configurado (valor ocultado)")
    else:
        print("Modo Testcontainers ativo sem .env: usando configurações automáticas de desenvolvimento.")

    goal = "spring-boot:test-run" if use_testcontainers else "spring-boot:run"
    print()
    if use_testcontainers:
        print("🐳 Iniciando com Testcontainers (PostgreSQL dinâmico no Docker)...")
    else:
        print("🚀 Iniciando aplicação...")

    mvn_cmd = "./mvnw" if (backend_dir / "mvnw").is_file() else "mvn"
    env_vars = os.environ.copy()
    if "SPRING_PROFILES_ACTIVE" not in env_vars:
        env_vars["SPRING_PROFILES_ACTIVE"] = "dev"

    try:
        return subprocess.run(
            [mvn_cmd, goal],
            cwd=backend_dir,
            env=env_vars,
            check=False,
        ).returncode
    except FileNotFoundError:
        print(f"Comando {mvn_cmd} não encontrado.", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
