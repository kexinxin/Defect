from sklearn import linear_model
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
from sklearn.model_selection import KFold, StratifiedKFold
import numpy as np
# x = np.array([[1, 2], [3, 4], [1, 2], [3, 4],[1, 2], [3, 4], [1, 2], [3, 4],[1, 2], [3, 4], [1, 2], [3, 4],[1, 2], [3, 4], [1, 2], [3, 4]])
# y = np.array([1, 0, 1, 0,1, 0, 0, 0,1, 0, 1, 0,1, 0, 0, 0])
from BorderlineOverSamplling import BorderlineOverSampling
from ConfusionMatrix import ConfusionMatrix
from Imahakil import IMAHAKIL
from adasyn import Adasyn
from mahakil import MAHAKIL
from smote import Smote

project="kc1.txt"
dataset = np.loadtxt('I:\\tools\\SoftwarePrediction\\dataset\\'+project, delimiter=",")
length=len(dataset[0])
x = dataset[:,0:length-1]
y = dataset[:,length-1]
y[y>=1]=1
acc=[]
precision=[]
recall=[]
f1=[]
gMean=[]
matrix=[]
N=5
count=1
for i in range(N):
    skf = StratifiedKFold(n_splits=10,shuffle=True)
    for train_index, test_index in skf.split(x,y):
        #print("TRAIN:", train_index, "TEST:", test_index)
        x_train, x_test = x[train_index], x[test_index]
        y_train, y_test = y[train_index], y[test_index]

        #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
        #x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
        #x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
        #x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
        #x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()


        clf = linear_model.LogisticRegression(solver='liblinear',max_iter=10000)
        clf.fit(x_train, y_train)

        y_pred = clf.predict(x_test)
        acc.append(accuracy_score(y_test, y_pred))
        precision.append( precision_score(y_test, y_pred))
        recall.append(recall_score(y_test, y_pred))
        f1.append(f1_score(y_test, y_pred))
        tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
        matrix.append(ConfusionMatrix(tn,fp,fn,tp))
        gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)
        print(count)
        count=count+1
        # print("acc", accuracy_score(y_test, y_pred))
        # print("precision", precision_score(y_test, y_pred))
        # print("recall", recall_score(y_test, y_pred))
        # print("f1", f1_score(y_test, y_pred))
acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())
doc = open("test.txt", 'a')
doc.write("project:"+project+" "+"algorithm:LR "+"sampleMethod: \n")
doc.write("-----------------------")

doc.write("-----------------------")
doc.close()