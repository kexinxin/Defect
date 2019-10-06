# -*- coding: utf-8 -*-
import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn import naive_bayes
from sklearn import linear_model

from Imahakil import IMAHAKIL
from adasyn import Adasyn
from smote import Smote

dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\dataset\\pc1.txt', delimiter=",")
length=len(dataset[0])
x = dataset[:,0:length-1]
y = dataset[:,length-1]
x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4)

x_train, y_train=Adasyn(x_train,y_train,0.5,10).sampling()


clf = linear_model.LogisticRegression(solver='liblinear',max_iter=10000)
clf.fit(x_train,y_train)

y_pred=clf.predict(x_test)
# count=0
# # for i in range(len(result)):
# #     if result[i]==y_test[i]:
# #         count=count+1
# # print(count/len(result))
print("acc", accuracy_score(y_test, y_pred))
for i in range(len(y_pred)):
    if y_test[i] == 1 and y_pred[i] == 1:
        print(i)
print("precision", precision_score(y_test, y_pred))
print("recall", recall_score(y_test, y_pred))
print("f1", f1_score(y_test, y_pred))