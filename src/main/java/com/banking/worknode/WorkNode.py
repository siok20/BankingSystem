import socket
import sys
import threading
import os
import glob
from datetime import datetime

def handle_client(conn, addr, port):
    id_socket = port % 8081
    carpeta_path = f"src/main/data/nodo{id_socket}"

    try:
        data = conn.recv(1024).decode('utf-8').strip()
        print(f"Nodo en puerto {port} recibió: {data}")
        parts = data.split(",")
        operacion = parts[0]
        datos = list(map(int, parts[1:]))

        if reject(operacion, datos, id_socket, carpeta_path):
            conn.sendall("REJECTED\n".encode('utf-8'))
            conn.close()
            print(f"Nodo en puerto {port} rechazó la conexión.")
            return

        result = process_operation(operacion, datos, carpeta_path)
        conn.sendall((result + "\n").encode('utf-8'))
        # Simula procesamiento
        import time
        time.sleep(1)
        conn.sendall(f"Nodo {port} procesó: {data}\n".encode('utf-8'))

    except Exception as e:
        print("Error:", e)
    finally:
        conn.close()

def process_operation(operacion, datos, carpeta_path):
    if operacion == "CONSULTAR_SALDO":
        id = datos[0]
        for file_path in glob.glob(os.path.join(carpeta_path, "cuenta.*.txt")):
            with open(file_path, 'r') as f:
                for line in f:
                    partes = [p.strip() for p in line.split("|")]
                    if len(partes) >= 3:
                        try:
                            numero = int(partes[0])
                            if numero == id:
                                return f"SALDO: {partes[2]}"
                        except ValueError:
                            continue
        return "REJECTED: Cuenta no encontrada"

    elif operacion == "TRANSFERIR_FONDOS":
        id1, id2, monto = datos
        if monto <= 0 or id1 == id2:
            return "REJECTED: Datos inválidos"

        cuenta1 = cuenta2 = None
        saldo1 = saldo2 = None
        path1 = path2 = None

        for file_path in glob.glob(os.path.join(carpeta_path, "cuenta.*.txt")):
            with open(file_path, 'r') as f:
                lines = f.readlines()
            for i, line in enumerate(lines):
                partes = [p.strip() for p in line.split("|")]
                if len(partes) >= 3:
                    try:
                        num = int(partes[0])
                        saldo = int(float(partes[2]))
                        if num == id1:
                            cuenta1 = (lines, i, partes)
                            saldo1 = saldo
                            path1 = file_path
                        elif num == id2:
                            cuenta2 = (lines, i, partes)
                            saldo2 = saldo
                            path2 = file_path
                    except ValueError:
                        continue

        if saldo1 is None or saldo2 is None:
            return "REJECTED: Cuenta no encontrada"
        if saldo1 < monto:
            return "REJECTED: Saldo insuficiente"

        # Actualizar saldos
        cuenta1[2][2] = str(saldo1 - monto)
        cuenta2[2][2] = str(saldo2 + monto)
        cuenta1[0][cuenta1[1]] = " | ".join(cuenta1[2]) + "\n"
        cuenta2[0][cuenta2[1]] = " | ".join(cuenta2[2]) + "\n"

        with open(path1, 'w') as f:
            f.writelines(cuenta1[0])
        if path1 != path2:
            with open(path2, 'w') as f:
                f.writelines(cuenta2[0])

        return f"{id1} | {id2} | {monto} | {obtener_fecha_hora()}?SUCCESS: Transferencia realizada"

    return "ERROR"

def reject(operacion, valores, id_socket, carpeta_path):
    ids = []
    if operacion == "CONSULTAR_SALDO":
        ids.append(valores[0])
    elif operacion == "TRANSFERIR_FONDOS":
        ids.extend(valores[:2])
        if valores[2] <= 0:
            return True

    encontrados = 0
    for file_path in glob.glob(os.path.join(carpeta_path, "cuenta.*.txt")):
        with open(file_path, 'r') as f:
            for line in f:
                partes = [p.strip() for p in line.split("|")]
                if partes and partes[0].isdigit():
                    if int(partes[0]) in ids:
                        encontrados += 1
        if encontrados == len(ids):
            return False
    return False

def obtener_fecha_hora():
    return datetime.now().strftime('%Y-%m-%d %H:%M:%S')

def start_server(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind(('localhost', port))
        s.listen()
        print(f"Nodo Python escuchando en puerto {port}")
        while True:
            conn, addr = s.accept()
            threading.Thread(target=handle_client, args=(conn, addr, port)).start()

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Uso: python3 worknode.py <puerto>")
        sys.exit(1)
    port = int(sys.argv[1])
    start_server(port)
