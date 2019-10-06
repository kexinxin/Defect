from sklearn import svm
from sklearn.metrics import f1_score,accuracy_score,precision_score
import numpy as np
from sklearn.model_selection import KFold
from sklearn.ensemble import RandomForestClassifier

dataset = np.loadtxt('I:\\tools\\genetic-hyperopt\\dataset\\kc2.txt', delimiter=",")
length = len(dataset[0])
X = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
y[y>=1]=1
indices = [(train_ind, val_ind) for (train_ind, val_ind) in KFold(n_splits=5,shuffle=True,random_state=2).split(X, y)]

score = 0
for train_ind, val_ind in indices:
    clf=RandomForestClassifier(min_samples_leaf=4,n_estimators=17,max_features="log2",criterion="entropy",bootstrap=False)
    clf.fit(X[train_ind], y[train_ind])
    print(f1_score(clf.predict(X[val_ind]), y[val_ind]))
    score+=f1_score(clf.predict(X[val_ind]), y[val_ind])
print("___________")
print(score/5)
