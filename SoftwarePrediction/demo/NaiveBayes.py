# -*- coding: utf-8 -*-
import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn import naive_bayes

from Imahakil import IMAHAKIL

dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\dataset\\cm1.txt', delimiter=",")
length=len(dataset[0])
x = dataset[:,0:length-1]
y = dataset[:,length-1]
x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4)

#x_train, y_train=IMAHAKIL().fit_sample(x_train, y_train)

x_train, y_train=IMAHAKIL().fit_sample(x_train, y_train)

clf=naive_bayes.GaussianNB()  #高斯分布，没有参数
# clf=naive_bayes.MultinomialNB()  #多项式分布
clf.fit(x_train,y_train)
y_pred=clf.predict(x_test)
# count=0
# for i in range(len(result)):
#     if result[i]==y_test[i]:
#         count=count+1
# print(count/len(result))
print("total test", len(y_pred))
print("true pred", len(y_pred[y_pred == y_test]))
print("acc", accuracy_score(y_test, y_pred))
for i in range(len(y_pred)):
    if y_test[i] == 1 and y_pred[i] == 1:
        print(i)
print("precision", precision_score(y_test, y_pred))
print("recall", recall_score(y_test, y_pred))
print("f1", f1_score(y_test, y_pred))
print("auc",)