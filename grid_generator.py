from random import *
import sys

# python gen.py grid_x grid_y n_epochs n_users > output.in

grid_x = int(sys.argv[1])  # randint(1, 10)
grid_y = int(sys.argv[2])  # randint(1, 10)
n_epochs = int(sys.argv[3])  # randint(1, 10)
n_users = int(sys.argv[4])  # randint(1, 10)

G = list()

i = 0
while i < n_epochs:
    u = 0
    while u < n_users:
        x = randint(0, grid_x)
        y = randint(0, grid_y)
        G.append([x,y])
        if (x, y) not in G:
            print("user"+str(u), i ,x, y)
            u += 1    
    i += 1