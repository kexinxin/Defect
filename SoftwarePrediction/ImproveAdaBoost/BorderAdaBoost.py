# -*- coding: utf-8 -*-

# coding: UTF-8
import math
import random

import numpy as np
from ImproveAdaBoost.WeakClassify import DecisionStump
from sklearn.metrics import accuracy_score
from ImproveAdaBoost.Relation import Relation


class BorderAdaBoost:
    def __init__(self, X, y, Weaker=DecisionStump):
        self.X = np.array(X)
        self.y = np.array(y).flatten(1)
        self.Weaker = Weaker

        '''
        W为权值，初试情况为均匀分布，即所有样本都为1/n
        '''
        # self.W=np.ones((self.X.shape[1],1)).flatten(1)/self.X.shape[1]
        # self.sums = np.zeros(self.y.shape)
        self.W = None
        self.sums = None
        self.distanceArray = None
        self.Q = 0  # 弱分类器的实际个数

    # M 为弱分类器的最大数量，可以在main函数中修改

    def train(self, M=5):
        self.G = {}  # 表示弱分类器的字典
        self.alpha = {}  # 每个弱分类器的参数
        for i in range(M):
            self.G.setdefault(i)
            self.alpha.setdefault(i)


        originDataX = self.X;
        originDataY = self.y;
        size = originDataX.shape[1];
        if self.distanceArray is None:
            self.distanceArray = np.zeros((size, size), dtype=float)
            for i in range(size):
                for j in range(size):
                    if j > i:
                        self.distanceArray[i][j] = np.sqrt(np.sum(np.square(originDataX[:, i] - originDataX[:, j])))
                        self.distanceArray[j][i] = self.distanceArray[i][j]
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
                    if originDataY[j] == -1:
                        minknear[count].append(Relation(i, j, self.distanceArray[i][j]))
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
                                                 self.distanceArray[i][knear[index].get_majorNumber()]))
                maxknear = sorted(maxknear, key=lambda distance: distance.get_distance())
                # 设置边界节点的权重
                for i in range(k):
                    if maxknear[i].get_minNumber() == knear[index].get_minNumber():
                        kLink[knear[index].get_minNumber()] = kLink[knear[index].get_minNumber()] + 1

        # 标准化
        total = 0
        for i in range(size):
            total += kLink[i]
        for i in range(size):
            kLink[i] = kLink[i] / total

        # for i in range(size):
        #     print(kLink[i])

        # 要生成的个数N
        N = count
        # N=0
        T = []
        for i in range(size):
            T.append(math.ceil(N * kLink[i]))


        for iClassfiler in range(M):  # self.G[i]为第i个弱分类器
            # for i in range(size):
            #     print(T[i])
            originDataX = self.X;
            originDataY = self.y;
            sampleDataCount = 0
            # 使用SMOT人工合成过采样技术生成少数类
            for i in range(size):
                if T[i] != 0:
                    orinMin = originDataX[:, i]
                    minNearArray = []
                    for j in range(size):
                        if j != i and originDataY[j] == 1:
                            minNearArray.append(Relation(i, j, self.distanceArray[i][j]))
                    minNearArray = sorted(minNearArray, key=lambda distance: distance.get_distance())
                    for j in range(T[i]):
                        nearData = originDataX[:, minNearArray[j].get_majorNumber()]
                        sampleData = (orinMin - nearData) * random.random() + orinMin
                        sampleData = sampleData.reshape(1, originDataX.shape[0])
                        originDataX = np.concatenate((originDataX, sampleData.T), axis=1)
                        originDataY = np.concatenate((originDataY, np.array([1])), axis=0)
                        sampleDataCount = sampleDataCount + 1
                        # print(sampleData)

            originTotalWeight = 0
            if self.W is None:
                self.W = np.ones((originDataX.shape[1], 1)).flatten(1) / originDataX.shape[1]
                self.sums = np.zeros(originDataY.shape)
            else:
                for i in range(size):
                    originTotalWeight = originTotalWeight + self.W[i]
                # self.W=np.delete(self.W,[size:],axis=0)
                self.W = self.W[:size]
                for i in range(sampleDataCount):
                    self.W = np.concatenate((self.W, np.array([(1 - originTotalWeight) / (0.94*sampleDataCount)])), axis=0)
                # print("")
                # for i in range(sampleDataCount):
                #     self.W[i+size]=sampleDataCount/sampleDataCount

            self.G[iClassfiler] = self.Weaker(originDataX, originDataY)
            e = self.G[iClassfiler].train(self.W)  # 根据当前权值进行该个弱分类器训练

            self.alpha[iClassfiler] = 1.0 / 2 * np.log((1 - e) / e)  # 计算该分类器的系数
            res = self.G[iClassfiler].pred(originDataX)  # res表示该分类器得出的输出

            # 计算当前次数训练精确度
            print("weak classfier acc", accuracy_score(originDataY, res),
                  "\n======================================================")

            # Z表示规范化因子
            Z = self.W * np.exp(-self.alpha[iClassfiler] * originDataY * res.transpose())
            self.W = (Z / Z.sum()).flatten(1)  # 更新权值
            self.Q = iClassfiler
            # errorcnt返回分错的点的数量，为0则表示perfect
            if (self.errorcnt(iClassfiler, originDataX, originDataY) == 0):
                print("%d个弱分类器可以将错误率降到0" % (iClassfiler + 1))
                break

    def errorcnt(self, t, originDataX, originDataY):  # 返回错误分类的点
        self.sums = self.sums + self.G[t].pred(originDataX).flatten(1) * self.alpha[t]

        pre_y = np.zeros(np.array(self.sums).shape)
        pre_y[self.sums >= 0] = 1
        pre_y[self.sums < 0] = -1

        t = (pre_y != originDataY).sum()
        return t

    def pred(self, test_X):  # 测试最终的分类器
        test_X = np.array(test_X)
        sums = np.zeros(test_X.shape[1])
        for i in range(self.Q + 1):
            sums = sums + self.G[i].pred(test_X).flatten(1) * self.alpha[i]
        pre_y = np.zeros(np.array(sums).shape)
        pre_y[sums >= 0] = 1
        pre_y[sums < 0] = -1
        return pre_y
