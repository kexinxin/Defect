from DataSetTool import DataSetTool
from Imahakil import IMAHAKIL
from Smahakil import Smahakil
import numpy as np

from mahakil import MAHAKIL

fileName="I:\\tools\\SoftwarePrediction\\dataset\\kc2.txt"
dataset = np.loadtxt(fileName, delimiter=",")
length = len(dataset[0])

data_list, label_list = DataSetTool.init_data(fileName,length-1,False)
data = data_list[0]
label = label_list[0]
data_new, label_new=Smahakil().fit_sample(data, label)
label_new=label_new.reshape(1,label_new.shape[0]).T
data_new=np.concatenate((data_new,label_new),axis=1)
# doc = open("test.txt", 'w')
# for row in data_new:
#     doc.write(str(int(row[0])) + "," + str(int(row[1])) + "," + str(int(row[2])) + "\n")
# doc.close()
