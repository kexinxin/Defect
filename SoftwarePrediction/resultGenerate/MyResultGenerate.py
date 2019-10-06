from sklearn import linear_model
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
from sklearn.model_selection import KFold, StratifiedKFold, train_test_split
import numpy as np

from BorderlineOverSamplling import BorderlineOverSampling
from ConfusionMatrix import ConfusionMatrix
from Imahakil import IMAHAKIL
from adasyn import Adasyn
from mahakil import MAHAKIL
from smote import Smote
from sklearn import linear_model, naive_bayes, tree
project="kc2.txt"
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


# test_size=0.4,random_state=8/test_size=0.2,random_state=5/0.2,25
before_x_train, x_test, before_y_train, y_test = train_test_split(x, y, test_size=0.2,random_state=25)
pfp=0.568


x_train=before_x_train
y_train=before_y_train


doc = open("generate.txt", 'w')
doc.write("##################################None############################################"+"\n")
for i in range(len(x_train)):
    for j in range(len(x_train[i])):
        doc.write(str(x_train[i][j]) + ",")
    doc.write(str(y_train[i]) + "\n")




#x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()

#clf = linear_model.LogisticRegression(solver='liblinear',max_iter=10000)
#clf=tree.DecisionTreeClassifier(max_depth=None, min_samples_split=2,random_state=0)

#clf=RandomForestClassifier(n_estimators=10, max_depth=None,min_samples_split=2, random_state=2)
clf=RandomForestClassifier(n_estimators=10, max_depth=None,min_samples_split=3, random_state=2)

clf.fit(x_train, y_train)

y_pred = clf.predict(x_test)
acc.append(accuracy_score(y_test, y_pred))
precision.append( precision_score(y_test, y_pred))
recall.append(recall_score(y_test, y_pred))
f1.append(f1_score(y_test, y_pred))
tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
matrix.append(ConfusionMatrix(tn,fp,fn,tp))
gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)


acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("##################################None#########################################")
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())
#################################################################################
acc=[]
precision=[]
recall=[]
f1=[]
gMean=[]
matrix=[]
#x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4,random_state=3)
x_train, y_train = BorderlineOverSampling(before_x_train, before_y_train, pfp, 5).sampling()
doc.write("\n"+"\n"+"\n"+"\n"+"\n"+"##################################BorderlineOverSampling############################################"+"\n")
for i in range(len(x_train)):
    for j in range(len(x_train[i])):
        doc.write(str(round(x_train[i][j],2))+ ",")
    doc.write(str(y_train[i]) + "\n")

#x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()

clf.fit(x_train, y_train)

y_pred = clf.predict(x_test)
acc.append(accuracy_score(y_test, y_pred))
precision.append( precision_score(y_test, y_pred))
recall.append(recall_score(y_test, y_pred))
f1.append(f1_score(y_test, y_pred))
tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
matrix.append(ConfusionMatrix(tn,fp,fn,tp))
gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)


acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("##################################BorderlineOverSampling#########################################")
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())
#################################################################################
acc=[]
precision=[]
recall=[]
f1=[]
gMean=[]
matrix=[]
#x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4,random_state=3)
#x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()


x_train, y_train = Adasyn(before_x_train, before_y_train, pfp, 10).sampling()
doc.write("\n"+"\n"+"\n"+"\n"+"\n"+"##################################Adasyn############################################"+"\n")
for i in range(len(x_train)):
    for j in range(len(x_train[i])):
        doc.write(str(round(x_train[i][j],2))+ ",")
    doc.write(str(y_train[i]) + "\n")

#x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()


clf.fit(x_train, y_train)

y_pred = clf.predict(x_test)
acc.append(accuracy_score(y_test, y_pred))
precision.append( precision_score(y_test, y_pred))
recall.append(recall_score(y_test, y_pred))
f1.append(f1_score(y_test, y_pred))
tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
matrix.append(ConfusionMatrix(tn,fp,fn,tp))
gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)


acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("##################################Adasyn#########################################")
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())
#################################################################################
acc=[]
precision=[]
recall=[]
f1=[]
gMean=[]
matrix=[]
#x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4,random_state=3)
#x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
x_train, y_train = Smote(before_x_train, before_y_train, pfp, 10).over_sampling()
doc.write("\n"+"\n"+"\n"+"\n"+"\n"+"##################################Smote############################################"+"\n")
for i in range(len(x_train)):
    for j in range(len(x_train[i])):
        doc.write(str(round(x_train[i][j],2))+ ",")
    doc.write(str(y_train[i]) + "\n")


clf.fit(x_train, y_train)

y_pred = clf.predict(x_test)
acc.append(accuracy_score(y_test, y_pred))
precision.append( precision_score(y_test, y_pred))
recall.append(recall_score(y_test, y_pred))
f1.append(f1_score(y_test, y_pred))
tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
matrix.append(ConfusionMatrix(tn,fp,fn,tp))
gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)


acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("##################################Smote#########################################")
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())
#################################################################################
acc=[]
precision=[]
recall=[]
f1=[]
gMean=[]
matrix=[]
#x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4,random_state=3)
#x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
x_train, y_train = MAHAKIL(pfp).fit_sample(before_x_train, before_y_train)
doc.write("\n"+"\n"+"\n"+"\n"+"\n"+"##################################MAHAKIL############################################"+"\n")
for i in range(len(x_train)):
    for j in range(len(x_train[i])):
        doc.write(str(round(x_train[i][j],2))+ ",")
    doc.write(str(y_train[i]) + "\n")

#x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
#x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()


clf.fit(x_train, y_train)

y_pred = clf.predict(x_test)
acc.append(accuracy_score(y_test, y_pred))
precision.append( precision_score(y_test, y_pred))
recall.append(recall_score(y_test, y_pred))
f1.append(f1_score(y_test, y_pred))
tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
matrix.append(ConfusionMatrix(tn,fp,fn,tp))
gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)


acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("##################################MAHAKIL#########################################")
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())


#################################################################################
acc=[]
precision=[]
recall=[]
f1=[]
gMean=[]
matrix=[]
#random_state 3,8
#x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4,random_state=20)
#x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
#x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
x_train, y_train = IMAHAKIL(pfp).fit_sample(before_x_train, before_y_train)


doc.write("\n"+"\n"+"\n"+"\n"+"\n"+"##################################IMAHAKIL############################################"+"\n")
for i in range(len(x_train)):
    for j in range(len(x_train[i])):
        doc.write(str(round(x_train[i][j],2))+ ",")
    doc.write(str(y_train[i]) + "\n")


#x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()


clf.fit(x_train, y_train)

y_pred = clf.predict(x_test)
acc.append(accuracy_score(y_test, y_pred))
precision.append( precision_score(y_test, y_pred))
recall.append(recall_score(y_test, y_pred))
f1.append(f1_score(y_test, y_pred))
tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
matrix.append(ConfusionMatrix(tn,fp,fn,tp))
gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)


acc=np.array(acc)
precision=np.array(precision)
recall=np.array(recall)
f1=np.array(f1)
gMean=np.array(gMean)
print("##################################IMAHAKIL#########################################")
print("acc",acc.mean())
print("precision",precision.mean())
print("recall",recall.mean())
print("f1",f1.mean())
print("gMean",gMean.mean())

# doc = open("test.txt", 'a')
# doc.write("project:"+project+" "+"algorithm:LR "+"sampleMethod: \n")
# doc.write("-----------------------")
# doc.write("-----------------------")
# doc.close()



doc.write("\n"+"\n"+"\n"+"\n"+"\n"+"##################################TEST############################################"+"\n")
for i in range(len(x_test)):
    for j in range(len(x_test[i])):
        doc.write(str(round(x_test[i][j],2))+ ",")
    doc.write(str(y_test[i]) + "\n")

doc.close()