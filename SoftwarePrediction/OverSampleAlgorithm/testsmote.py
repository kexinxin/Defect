from smote import Smote
import numpy as np
from sklearn.model_selection import train_test_split

dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\dataset\\kc2.txt', delimiter=",")
length = len(dataset[0])
x = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
x_train = x;
y_train = y
data_new,label_new=Smote(x_train,y_train,90,5).over_sampling()

label_new=label_new.reshape(1,label_new.shape[0]).T
data_new=np.concatenate((data_new,label_new),axis=1)
doc = open("test.txt", 'w')
for row in data_new:
    doc.write(str(int(row[0])) + "," + str(int(row[1])) + "," + str(int(row[2])) + "\n")
doc.close()