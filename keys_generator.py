import sys

from Crypto.PublicKey import RSA


def store_key(output_file_name, key):
    with open(output_file_name, 'w') as fp:
        fp.write(key)

if(len(sys.argv) != 1):
    print("python gen.py number_of_keys_to_generate")
    sys.exit()

key = RSA.generate(4096)
pubkey = key.publickey()

n_users = int(sys.argv[1])  # number of keys

print("Started generating keys.")
id = 0
while i < n_users:
    store_key("/keys/priv_" + id + ".key", key.exportKey('PEM'))
    store_key("/keys/pub_" + id + ".key", pubkey.exportKey('OpenSSH'))
    id+=1

print("finished generating keys")




