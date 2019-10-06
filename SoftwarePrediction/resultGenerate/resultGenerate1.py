from sklearn import linear_model, naive_bayes, tree
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
from sklearn.model_selection import KFold, StratifiedKFold
import numpy as np
# x = np.array([[1, 2], [3, 4], [1, 2], [3, 4],[1, 2], [3, 4], [1, 2], [3, 4],[1, 2], [3, 4], [1, 2], [3, 4],[1, 2], [3, 4], [1, 2], [3, 4]])
# y = np.array([1, 0, 1, 0,1, 0, 0, 0,1, 0, 1, 0,1, 0, 0, 0])
from sklearn.neighbors import KNeighborsClassifier

from BorderlineOverSamplling import BorderlineOverSampling
from ConfusionMatrix import ConfusionMatrix
from Imahakil import IMAHAKIL
from adasyn import Adasyn
from mahakil import MAHAKIL
from smote import Smote


projectList=['ant-1.7.csv','camel-1.4.csv','ivy-1.4.csv','ivy-2.0.csv','xerces-1.2.csv','xerces-1.3.csv','kc1.txt','kc2.txt','pc1.txt']
projectDirPath='I:\\tools\\SoftwarePrediction\\dataset\\'
algorithmName="CART."
clf=tree.DecisionTreeClassifier(criterion='entropy')
for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"IMAHAKIL", 'a')
    doc.write("project:"+project+" "+"algorithm: "+algorithmName+"sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
            #x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
            #x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
            x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
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
            print(str(count)+","+project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"MAHAKIL", 'a')
    doc.write("project:"+project+" "+"algorithm:"+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
            #x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
            x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
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
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"Borderline", 'a')
    doc.write("project:"+project+" "+"algorithm:"+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
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
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"ADASYN", 'a')
    doc.write("project:"+project+" "+"algorithm:"+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
            x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
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
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"SMOTE", 'a')
    doc.write("project:"+project+" "+"algorithm: "+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
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
            x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()

            clf.fit(x_train, y_train)

            y_pred = clf.predict(x_test)
            acc.append(accuracy_score(y_test, y_pred))
            precision.append( precision_score(y_test, y_pred))
            recall.append(recall_score(y_test, y_pred))
            f1.append(f1_score(y_test, y_pred))
            tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
            matrix.append(ConfusionMatrix(tn,fp,fn,tp))
            gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

##############################################################################################################################################################################################


algorithmName="KNN."
clf=KNeighborsClassifier()
for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"IMAHAKIL", 'a')
    doc.write("project:"+project+" "+"algorithm: "+algorithmName+"sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
            #x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
            #x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
            x_train, y_train = IMAHAKIL().fit_sample(x_train, y_train)
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
            print(str(count)+","+project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"MAHAKIL", 'a')
    doc.write("project:"+project+" "+"algorithm:"+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
            #x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
            x_train, y_train = MAHAKIL().fit_sample(x_train, y_train)
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
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"Borderline", 'a')
    doc.write("project:"+project+" "+"algorithm:"+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
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
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"ADASYN", 'a')
    doc.write("project:"+project+" "+"algorithm:"+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
    for i in range(N):
        skf = StratifiedKFold(n_splits=10,shuffle=True)
        for train_index, test_index in skf.split(x,y):
            #print("TRAIN:", train_index, "TEST:", test_index)
            x_train, x_test = x[train_index], x[test_index]
            y_train, y_test = y[train_index], y[test_index]

            #x_train, y_train = BorderlineOverSampling(x_train, y_train, 0.5, 10).sampling()
            x_train, y_train = Adasyn(x_train, y_train, 0.5, 10).sampling()
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
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()

for project in projectList:
#project="kc2.txt"
    dataset = np.loadtxt(projectDirPath+project, delimiter=",")
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
    N=20
    count=1
    doc = open(project+algorithmName+"SMOTE", 'a')
    doc.write("project:"+project+" "+"algorithm: "+algorithmName+" sampleMethod: \n")
    doc.write("-----------------------\n")
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
            x_train, y_train = Smote(x_train, y_train, 50, 10).over_sampling()

            clf.fit(x_train, y_train)

            y_pred = clf.predict(x_test)
            acc.append(accuracy_score(y_test, y_pred))
            precision.append( precision_score(y_test, y_pred))
            recall.append(recall_score(y_test, y_pred))
            f1.append(f1_score(y_test, y_pred))
            tn, fp, fn, tp=confusion_matrix(y_test,y_pred).ravel()
            matrix.append(ConfusionMatrix(tn,fp,fn,tp))
            gMean.append(((tp/(tp+fn))*((tn)/(fp+tn)))**0.5)
            #print(count)
            print(str(count) + "," + project)
            count=count+1

        acc = np.array(acc)
        precision = np.array(precision)
        recall = np.array(recall)
        f1 = np.array(f1)
        gMean = np.array(gMean)

        doc.write("acc:")
        for i in range(len(acc)-1):
            doc.write(str(acc[i])+",")
        doc.write(str(acc[len(acc)-1]))
        doc.write("\n")

        doc.write("precision:")
        for i in range(len(precision) - 1):
            doc.write(str(precision[i]) + ",")
        doc.write(str(precision[len(precision) - 1]))
        doc.write("\n")

        doc.write("recall:")
        for i in range(len(recall) - 1):
            doc.write(str(recall[i]) + ",")
        doc.write(str(recall[len(recall) - 1]))
        doc.write("\n")

        doc.write("f1:")
        for i in range(len(f1) - 1):
            doc.write(str(f1[i]) + ",")
        doc.write(str(f1[len(f1) - 1]))
        doc.write("\n")

        doc.write("gMean:")
        for i in range(len(gMean) - 1):
            doc.write(str(gMean[i]) + ",")
        doc.write(str(gMean[len(gMean) - 1]))
        doc.write("\n")

        doc.write("matrix:")
        for i in range(len(matrix) - 1):
            doc.write(str(matrix[i]) + ",")
        doc.write(str(matrix[len(matrix) - 1]))
        doc.write("\n")

        print("acc", acc.mean())
        print("precision", precision.mean())
        print("recall", recall.mean())
        print("f1", f1.mean())
        print("gMean", gMean.mean())

        doc.write("avg:"+str(acc.mean())+","+str(precision.mean())+","+str(recall.mean())+","+str(f1.mean())+","+str(gMean.mean())+"\n")

        acc = []
        precision = []
        recall = []
        f1 = []
        gMean = []
        matrix = []
        doc.write("-----------------------\n")
    doc.close()