import sys

from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.backends import default_backend


def store_key(output_file_name, key):
    with open(output_file_name, 'w') as fp:
        fp.write(key)

n_users = int(sys.argv[1])  # number of keys

print("Started generating keys.")
id = 0
while id < n_users:
# generate private/public key pair
    key = rsa.generate_private_key(backend=default_backend(), public_exponent=65537, key_size=4096)

# get public key in OpenSSH format
    public_key = key.public_key().public_bytes(serialization.Encoding.OpenSSH, serialization.PublicFormat.OpenSSH)

# get private key in PEM container format
    pem = key.private_bytes(encoding=serialization.Encoding.PEM, format=serialization.PrivateFormat.TraditionalOpenSSL, encryption_algorithm=serialization.NoEncryption())

# decode to printable strings
    private_key_str = pem.decode('utf-8')
    public_key_str = public_key.decode('utf-8')

    store_key("keys/priv_" + str(id) + ".key", private_key_str)
    store_key("keys/pub_" + str(id) + ".key", public_key_str)

    id+=1

print("finished generating keys")



