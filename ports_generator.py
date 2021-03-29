from random import *
import sys

# python gen.py grid_x grid_y n_epochs n_users > output.in

n_users = int(sys.argv[1])  # randint(1, 10)

i = 0
port = 10000
while i < n_users:
    print("user"+ str(i) + ", localhost, " + str(port))
    port += 1
    i += 1
