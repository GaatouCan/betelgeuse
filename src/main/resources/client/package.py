import struct
import socket

MAGIC_NUMBER = 20241231
PACKAGE_VERSION = 1001

HEADER_SIZE = 24

class Package:
    class Header:
        magic       = MAGIC_NUMBER
        version     = PACKAGE_VERSION
        method      = 0
        resersh     = 0
        id          = 0
        size        = 0

    header_ = Header()
    data_ = bytearray()

    def set_id(self, id: int):
        self.header_.id = id
        return self
    
    def set_data(self, data: bytes):
        self.data_ = bytearray(data)
        self.header_.size = len(self.data_)
        return self
    
    def get_id(self):
        return self.header_.id
    
    def data_size(self):
        return self.header_.size
    
    def get_data(self):
        return self.data_.decode('utf-8')
    
    def get_raw_data(self):
        return self.data_
    
    def encode(self) -> bytes:
        return struct.pack(
            f'>IIHHIQ{self.header_.size}s', 
            self.header_.magic, 
            self.header_.version, 
            self.header_.method, 
            self.header_.resersh,
            self.header_.id,
            self.header_.size,
            self.data_
        )
    
    def decode(self, msg: bytes):
        header = struct.unpack('>IIHHIQ', msg[0:HEADER_SIZE])

        self.header_.magic = header[0]
        self.header_.version = header[1]
        self.header_.method = header[2]
        self.header_.id = header[4]
        self.header_.size = header[5]

        body = struct.unpack(f'{self.header_.size}s', msg[HEADER_SIZE:])
        self.data_ = body[0]
        
        return self
    
    def decodeWithSocket(self, sock: socket.socket):
        header = struct.unpack('>IIHHIQ', sock.recv(HEADER_SIZE))

        self.header_.magic = header[0]
        self.header_.version = header[1]
        self.header_.method = header[2]
        self.header_.id = header[4]
        self.header_.size = header[5]

        body = struct.unpack(f'{self.header_.size}s', sock.recv(self.header_.size))
        self.data_ = body[0]
        
        return self
    
if __name__ == "__main__":
    # print('Hello, world')

    pkg = Package()
    pkg.set_id(1321)
    pkg.set_data(b'Hello, world')

    content = pkg.encode()

    # print(content)

    tmp = Package()
    tmp.decode(content)

    print('magic: ', tmp.header_.magic)
    print('version: ', tmp.header_.version)
    print('method: ', tmp.header_.method)
    print('id: ', tmp.header_.id)
    print('data size: ', tmp.header_.size)

    print(tmp.get_data())
