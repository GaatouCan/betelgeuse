import socket
import time

from package import Package
from protobuf import player_pb2

SERVER_HOST = "localhost"
SERVER_PORT = 8080

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect((SERVER_HOST, SERVER_PORT))

# Login
req = player_pb2.ClientLoginRequest()

req.id = 1020144
req.token = 'Trust Me'

pkg = Package()
pkg.set_id(1001)
pkg.set_data(req.SerializeToString())

client.send(pkg.encode())

# Login Result
r_pkg = Package()
r_pkg.decodeWithSocket(client)

res = player_pb2.PlayerInfo()
res.ParseFromString(r_pkg.get_raw_data())

print(res)

time.sleep(2)

client.close()