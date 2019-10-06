import math

import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from sklearn import neighbors, datasets, linear_model
import pandas as pd

from sklearn.datasets.samples_generator import make_classification
# X为样本特征，y为样本类别输出， 共200个样本，每个样本2个特征，输出有3个类别，没有冗余特征，每个类别一个簇
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from sklearn.tree import tree


#X, y = make_classification(n_samples=200, n_features=2, n_redundant=0,n_clusters_per_class=1, n_classes=2)
from BorderlineOverSamplling import BorderlineOverSampling
from Imahakil import IMAHAKIL
from RandomOverSampler import RandomOverSampler
from adasyn import Adasyn
from mahakil import MAHAKIL
from smote import Smote


dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\testDataSet\\OneDimention', delimiter=",")
length = len(dataset[0])
X = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
x_train, x_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

#data_new,label_new=RandomOverSampler(x_train,y_train,90).samplping()
#data_new,label_new=Smote(x_train,y_train,90,5).over_sampling()

#data_new,label_new=Adasyn(x_train,y_train,0.9,10).sampling()
#data_new,label_new=BorderlineOverSampling(x_train,y_train,0.6,5).sampling()
#data_new,label_new=IMAHAKIL().fit_sample(x_train, y_train)
#data_new,label_new=MAHAKIL().fit_sample(x_train, y_train)
data_new=x_train;label_new=y_train

X=data_new
y=label_new
x_new=[]
y_new=[]
for i in range(len(y)):
    if y[i]==1:
        x_new.append(X[i])
        y_new.append(y[i])
X=np.array(x_new)
y=np.array(y_new)
X[:, 1]=0

myData=X[:,0]
interval=25
size=int(300/interval)

minknear = []
for i in range(size):
    minknear.append([])

TotalCount=0
for i in range(len(myData)):
    #print(myData[i])
    index=int(myData[i]/interval)
    if index<size:
        minknear[index].append(myData[i])
        TotalCount=TotalCount+1

diverceScore=0
for i in range(size):
    length=minknear[i].__len__()
    ration=length/TotalCount
    if ration>0:
        diverceScore+=ration*math.log(ration)

print(diverceScore)

#之所以生成2个特征值是因为需要在二维平面上可视化展示预测结果，所以只能是2个，3个都不行
cmap_bold = ListedColormap(['#FF0000', '#003300'])  # 给不同属性的点赋以颜色
plt.scatter(X[:, 0], X[:, 1], marker='o', c=y, cmap=cmap_bold)

# cmap_bold = ListedColormap(['#FF0000'])  # 给不同属性的点赋以颜色
# plt.scatter(X[:, 0], marker='o', c=y, cmap=cmap_bold)

plt.show() #根据随机生成样本不同，图形也不同