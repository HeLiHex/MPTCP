#!/bin/bash

sudo ip link add dummy0 type dummy
sudo ip addr add 1.0.0.0/24 dev dummy0
sudo ip link set dummy0 up

sudo ip link add dummy1 type dummy
sudo ip addr add 2.0.0.0/24 dev dummy1
sudo ip link set dummy1 up

# to remove a dummy interface
# sudo ip link delete dummy0