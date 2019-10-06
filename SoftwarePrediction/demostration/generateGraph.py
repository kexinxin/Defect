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


dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\testDataSet\\oringData3', delimiter=",")
length = len(dataset[0])
X = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
#6
x_train, x_test, y_train, y_test = train_test_split(X, y, test_size=0.2,random_state=8)

data_new,label_new=RandomOverSampler(x_train,y_train,90).samplping()
#data_new,label_new=Smote(x_train,y_train,90,5).over_sampling()

data_new,label_new=Adasyn(x_train,y_train,0.9,10).sampling()
#data_new,label_new=BorderlineOverSampling(x_train,y_train,0.6,4).sampling()
#data_new,label_new=IMAHAKIL().fit_sample(x_train, y_train)
#data_new,label_new=MAHAKIL().fit_sample(x_train, y_train)
#data_new=x_train;label_new=y_train

X=data_new
y=label_new


# label_new=label_new.reshape(1,label_new.shape[0]).T
# data_new=np.concatenate((data_new,label_new),axis=1)
# doc = open("test.txt", 'w')
# for row in data_new:
#     doc.write(str(int(row[0])) + "," + str(int(row[1])) + "," + str(int(row[2])) + "\n")
# doc.close()





#之所以生成2个特征值是因为需要在二维平面上可视化展示预测结果，所以只能是2个，3个都不行
cmap_bold = ListedColormap(['#FF0000', '#003300'])  # 给不同属性的点赋以颜色
plt.scatter(X[:, 0], X[:, 1], marker='o', c=y, cmap=cmap_bold)

# cmap_bold = ListedColormap(['#FF0000'])  # 给不同属性的点赋以颜色
# plt.scatter(X[:, 0], marker='o', c=y, cmap=cmap_bold)

plt.show() #根据随机生成样本不同，图形也不同

#clf = neighbors.KNeighborsClassifier(n_neighbors = 15 , weights='distance')
clf=linear_model.LogisticRegression(C=1e5)
#clf=RandomForestClassifier()
#clf=tree.DecisionTreeClassifier(criterion='entropy')
#clf=SVC(C=1.0, kernel='rbf', degree=3, gamma='auto', coef0=0.0, shrinking=True,probability=False, tol=0.001, cache_size=200, class_weight=None,verbose=False, max_iter=-1, decision_function_shape='ovr',random_state=None)

clf.fit(X, y)
h = 1  #网格中的步长
#确认训练集的边界
#生成随机数据来做测试集，然后作预测
x_min, x_max = X[:, 0].min() - 1, X[:, 0].max() + 1
y_min, y_max = X[:, 1].min() - 1, X[:, 1].max() + 1
xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                     np.arange(y_min, y_max, h)) #生成网格型二维数据对
Z = clf.predict(np.c_[xx.ravel(), yy.ravel()])




# Create color maps
cmap_light = ListedColormap(['#FFAAAA', '#AAFFAA'])  # 给不同区域赋以颜色
cmap_bold = ListedColormap(['#FF0000', '#003300'])  # 给不同属性的点赋以颜色
# 将预测的结果在平面坐标中画出其类别区域
Z = Z.reshape(xx.shape)
plt.figure()
plt.pcolormesh(xx, yy, Z, cmap=cmap_light)
# 也画出所有的训练集数据
plt.scatter(X[:, 0], X[:, 1], c=y, cmap=cmap_bold)
plt.xlim(xx.min(), xx.max())
plt.ylim(yy.min(), yy.max())
plt.show()

y_pred=clf.predict(x_test)
print("acc", accuracy_score(y_test, y_pred))
for i in range(len(y_pred)):
    if y_test[i] == 1 and y_pred[i] == 1:
        print(i)
print("precision", precision_score(y_test, y_pred))
print("recall", recall_score(y_test, y_pred))
print("f1", f1_score(y_test, y_pred))

