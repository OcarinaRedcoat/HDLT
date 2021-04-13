import sys

from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa


def store_key(output_file_name, key):
    with open(output_file_name, 'wb') as fp:
        fp.write(key)

def generate_and_write_keys(name_key):
    key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=4096
    )
    private_key = key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption())
    public_key = key.public_key().public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo)
    store_key("keys/priv_" + name_key + ".key", private_key)
    store_key("keys/pub_" + name_key + ".key", public_key)

n_users = int(sys.argv[1])  # number of keys
print("Started generating keys.")
generate_and_write_keys("server_1")
generate_and_write_keys("ha_1")
id = 0
while id < n_users:
    generate_and_write_keys("client_" + str(id))
    id+=1

print("finished generating keys")



