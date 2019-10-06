# -*- coding: utf-8 -*-
import math
import random

import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn import linear_model

from ImproveAdaBoost.Relation import Relation

dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\dataset\\cm1.txt', delimiter=",")
length=len(dataset[0])
x = dataset[:,0:length-1]
y = dataset[:,length-1]
x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4)


originDataX = x_train.T;
originDataY = y_train;
size = originDataX.shape[1];
tt = originDataX[:, 0]
distanceArray = np.zeros((size, size), dtype=float)
for i in range(size):
    for j in range(size):
        distanceArray[i][j] = np.sqrt(np.sum(np.square(originDataX[:, i] - originDataX[:, j])))
k = 10
minSize = 0
for i in range(size):
    if originDataY[i] == 1:
        minSize = minSize + 1
kLink = [0 for i in range(size)]
minknear = []  # 存储着所有有缺陷的模块到其他异类点之间的距离从小到大进行排序
count = 0  # 少数类个数
for i in range(size):
    if originDataY[i] == 1:  # 找出少数类
        minknear.append([])
        for j in range(size):
            if originDataY[j] == 0:
                minknear[count].append(Relation(i, j, distanceArray[i][j]))
        minknear[count] = sorted(minknear[count], key=lambda distance: distance.get_distance())
        count = count + 1
for knear in minknear:
    # 找出其最接近的k个异类元素
    for index in range(k):
        maxknear = []
        # 计算异类元素相邻的少数类元素
        for i in range(size):
            if originDataY[i] == 1:
                maxknear.append(Relation(i, knear[index].get_majorNumber(),
                                         distanceArray[i][knear[index].get_majorNumber()]))
        maxknear = sorted(maxknear, key=lambda distance: distance.get_distance())
        # 设置边界节点的权重
        for i in range(k):
            if maxknear[i].get_minNumber() == knear[index].get_minNumber():
                kLink[knear[index].get_minNumber()] = kLink[knear[index].get_minNumber()] + 1

# 调整边界节点的权重
k2 = 10
for knear in minknear:
    minNumber = knear[0].get_minNumber()
    minAllNear = []
    for i in range(size):
        minAllNear.append(Relation(minNumber, i, distanceArray[minNumber][i]))
    minAllNear = sorted(minAllNear, key=lambda distance: distance.get_distance())
    minCount = 0
    for i in range(k2):
        if originDataY[minAllNear[i].get_majorNumber()] == 1:
            minCount = minCount + 1
    kLink[minNumber] = kLink[minNumber] * (minCount / k2)

# for i in range(size):
#     print(kLink[i])

# 标准化
total = 0
for i in range(size):
    total += kLink[i]
for i in range(size):
    kLink[i] = kLink[i] / total

# for i in range(size):
#     print(kLink[i])

# 要生成的个数N
N = 2 * count
# N=0
T = []
for i in range(size):
    T.append(math.ceil(N * kLink[i]))

# for i in range(size):
#     print(T[i])

sampleDataCount = 0
# 使用SMOT人工合成过采样技术生成少数类
for i in range(size):
    if T[i] != 0:
        orinMin = originDataX[:, i]
        minNearArray = []
        for j in range(size):
            if j != i and originDataY[j] == 1:
                minNearArray.append(Relation(i, j, distanceArray[i][j]))
        minNearArray = sorted(minNearArray, key=lambda distance: distance.get_distance())
        for j in range(T[i]):
            nearData = originDataX[:, minNearArray[j].get_majorNumber()]
            sampleData = (orinMin - nearData) * random.random() + orinMin
            sampleData = sampleData.reshape(1, originDataX.shape[0])
            originDataX = np.concatenate((originDataX, sampleData.T), axis=1)
            originDataY = np.concatenate((originDataY, np.array([1])), axis=0)

            sampleDataCount = sampleDataCount + 1
            # print(sampleData)


clf = linear_model.LogisticRegression(solver='liblinear')
clf.fit(originDataX.T,originDataY)

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