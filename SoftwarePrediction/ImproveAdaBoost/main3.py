# -*- coding: utf-8 -*-
# coding: UTF-8

import numpy as np

from ImproveAdaBoost.AdaBoost import AdaBoost
from Imahakil import IMAHAKIL
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, f1_score, recall_score, precision_score


def main():

    # load data
    dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\dataset\\cm1.txt', delimiter=",")
    length = len(dataset[0])
    x = dataset[:, 0:length - 1]
    y = dataset[:, length - 1]

    # prepare train data
    x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4)
    #x_train=x;y_train=y
    #x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
    # prepare test and train data
    x_train=x_train.transpose()
    x_test=x_test.transpose()

    y_train[y_train == 1] = 1
    y_train[y_train == 0] = -1
    y_test[y_test == 1] = 1
    y_test[y_test == 0] = -1

    # train
    ada=AdaBoost(x_train, y_train)
    ada.train(50)

    # predict
    y_pred = ada.pred(x_test)
    print("total test", len(y_pred))
    print("true pred",  len(y_pred[y_pred == y_test]))
    print("acc", accuracy_score(y_test, y_pred))
    print("precision",precision_score(y_test,y_pred))
    print("recall",recall_score(y_test,y_pred))
    print("f1",f1_score(y_test,y_pred))
    for i in range(len(y_pred)):
        if y_test[i]==1 and y_pred[i]==1:
            print(i)
if __name__=='__main__':
    main()