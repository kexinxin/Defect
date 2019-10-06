import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import f1_score
from sklearn.model_selection import train_test_split
from sklearn.tree import tree

projectDirPath='I:\\tools\\SoftwarePrediction\\NASADataSet\\'
projectName=["CM1","JM1","KC1","KC3","KC4","MC1","MC2","MW1","PC1","PC2","PC3","PC4","PC5"]
f=open("I:\\tools\\SoftwarePrediction\\util\\BestFeature")
bestFeature=f.readline().split(",")
currentMetrics=[]
for i in range(len(bestFeature)):
    print('----------------------------------'+str(i)+'------------------------------------')
    currentMetrics.append(bestFeature[i])
    f1_list=[]
    for j in range(len(projectName)):
        project=projectName[j]
        f = open(projectDirPath + project+".csv")
        metrics = f.readline().split(",")
        indexList=[]
        for k in range(len(currentMetrics)):
            if(metrics.__contains__(currentMetrics[k])):
                index=metrics.index(currentMetrics[k])
                indexList.append(index)
        dataset = np.loadtxt(projectDirPath + project+".csv", delimiter=",", skiprows=1)
        if indexList.__len__()==0:
            indexList.append(metrics.__len__()-2)
        indexList.append(metrics.__len__()-1)
        dataset=dataset[:,indexList]
        length = len(dataset[0])
        x = dataset[:, 0:length - 1]
        y = dataset[:, length - 1]
        x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.4)
        #clf = tree.DecisionTreeClassifier(criterion='entropy')
        clf = RandomForestClassifier(min_samples_leaf=4, n_estimators=17, max_features="log2", criterion="entropy",bootstrap=False)
        clf.fit(x_train, y_train)
        y_pred = clf.predict(x_test)
        f1_value=f1_score(y_test, y_pred)
        f1_list.append(f1_value)
        #print(f1_value)
    total=0.0
    for j in range(len(f1_list)):
        total+=f1_list[j]
    print(total/13)