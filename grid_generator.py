from random import *

import sys
import json

#############################################################################
### python gen.py grid_size distance_allowed n_epochs n_users ###
#############################################################################

class PositionShort:
    def __init__(self, id, epoch, xPos, yPos):
        self.id = id
        self.epoch = epoch
        self.xPos = xPos
        self.yPos = yPos

class Position:
    def __init__(self, epoch, xPos, yPos):
        self.epoch = epoch
        self.xPos = xPos
        self.yPos = yPos
        self.closeBy = []

    def return_dict(self):
        dict = {"epoch": self.epoch, "xPos": self.xPos, "yPos": self.yPos, "closeBy": self.closeBy}
        return dict

    def addCloseBy(self, uid):
        self.closeBy.append(uid)

class User:
    def __init__(self, id):
        self.id = id
        self.ip = "localhost"
        self.port = 10000 + id
        self.positions = []

    def return_dict(self):
        positions = []
        for i in self.positions:
            positions.append(i.return_dict())
        dict = {"userId": self.id, "ip": self.ip, "port": self.port, "positions": positions}
        return dict

    def get_id(self):
        return self.id

class GridEpoch:
    def __init__(self):
        self.users = []

    def output_dict(self):
        dict = []
        for i in self.users:
            dict.append(i.return_dict())
        return dict

    def addUser(self, user):
        self.users.append(user)

def store_json(grids):
    output_file_name = 'grids.output.json'
    with open(output_file_name, 'w') as fp:
        json.dump(grids, fp)
        print("Stored grids in file: " + output_file_name)


epoch_correction = 2670

grid_size = int(sys.argv[1])  # randint(1, 10)
distance_allowed = int(sys.argv[2])  # randint(1, 10)
total_n_epochs = int(sys.argv[3]) + epoch_correction  # randint(1, 10)
n_users = int(sys.argv[4])  # randint(1, 10)


epoch_list = GridEpoch()
positionsSoFar = []
uid = 0

while uid < n_users:
    user = User(uid)

    positions = []
    current_epoch_n = epoch_correction
    while current_epoch_n < total_n_epochs:
        xPos = randint(0, grid_size)
        yPos = randint(0, grid_size)

        positionShort = PositionShort(user.id, current_epoch_n, xPos, yPos)
        position = Position(current_epoch_n, xPos, yPos)

        next = False
        for pos in positionsSoFar:
            if(pos.epoch != current_epoch_n):
                continue
            if(pos.xPos == xPos and pos.yPos == yPos):
                next = True
                break
        if(next):
            current_epoch_n -= 1
            continue

        positionsSoFar.append(positionShort)
        positions.append(position)

        current_epoch_n += 1

    user.positions = positions
    epoch_list.addUser(user)
    uid += 1


for user in epoch_list.users:
    for position in user.positions:
        for positionShort in positionsSoFar:
            if(user.id == positionShort.id):
                continue
            if(position.epoch == positionShort.epoch):
                if((abs(position.xPos - positionShort.xPos) <= distance_allowed) or (abs(position.yPos - positionShort.yPos) <= distance_allowed)):
                    position.addCloseBy(positionShort.id)




output_dic = epoch_list.output_dict()
store_json(output_dic)