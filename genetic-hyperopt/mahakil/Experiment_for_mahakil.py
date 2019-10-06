from DataSetTool import DataSetTool
from Imahakil import IMAHAKIL
import numpy as np

from mahakil import MAHAKIL

data_list, label_list = DataSetTool.init_data("I:\\tools\\SoftwarePrediction\\testDataSet\\oringData2.txt", 2,False)
data = data_list[0]
label = label_list[0]
data_new, label_new=MAHAKIL().fit_sample(data, label)
label_new=label_new.reshape(1,label_new.shape[0]).T
data_new=np.concatenate((data_new,label_new),axis=1)
# doc = open("test.txt", 'w')
# for row in data_new:
#     doc.write(str(int(row[0])) + "," + str(int(row[1])) + "," + str(int(row[2])) + "\n")
# doc.close()
