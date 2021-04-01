from random import *

import sys
import json

#############################################################################
### python gen.py grid_size distance_allowed n_epochs n_users ###
#############################################################################

class User:
    def __init__(self, id, epoch, xPos, yPos):
        self.id = id
        self.epoch = epoch
        self.xPos = xPos
        self.yPos = yPos
        self.closeBy = []

    def close(self, grid):
        if((abs(grid.xPos - self.xPos) <= distance_allowed) or (abs(grid.yPos - self.yPos) <= distance_allowed)):
            return True
        return False

    def return_dict(self):
        dict = {"userId": self.id, "epoch": self.epoch, "xPos": self.xPos, "yPos": self.yPos, "closeBy": self.closeBy}
        return dict

    def get_id(self):
        return self.id

    def add_closeBy(self, user):
            for i in range(0,len(self.closeBy)):
                if(self.closeBy[i] == user.get_id()):
                    return
            self.closeBy.append(user.get_id())

class GridEpoch:
    def __init__(self):
        self.users = []

    def add_user(self, user):
        for i in self.users:
            if(i.xPos == user.xPos and i.yPos == user.yPos):
                return False
        self.users.append(user)
        return True

    def output_dict(self):
        dict = []
        for i in self.users:
            dict.append(i.return_dict())
        return dict

    def calculateDistances(self):
        for i in self.users:
            for j in self.users:
                if(i.close(j) and i.epoch == j.epoch and j!=i):
                    i.add_closeBy(j)

def store_json(grids):
    output_file_name = 'grids.output.json'
    with open(output_file_name, 'w') as fp:
        json.dump(grids, fp)
        print("Stored grids in file: " + output_file_name)


grid_size = int(sys.argv[1])  # randint(1, 10)
distance_allowed = int(sys.argv[2])  # randint(1, 10)
n_epochs = int(sys.argv[3])  # randint(1, 10)
n_users = int(sys.argv[4])  # randint(1, 10)

epoch_list = GridEpoch()
i = 0

#TODO: remover isto. é so para comecar as epocas na grid mais á frente
i += 2670
n_epochs += 2670

while i < n_epochs:

    uid = 0
    while uid < n_users:
        user = User(uid, i, randint(0, grid_size), randint(0, grid_size))
        if(not epoch_list.add_user(user)):
            uid -= 1
        uid += 1
    i += 1

epoch_list.calculateDistances()
output_dic =  epoch_list.output_dict()
store_json(output_dic)



