import numpy as np
from sklearn.ensemble import ExtraTreesClassifier

from util.MetricsName import MetricsName

projectDirPath='I:\\tools\\SoftwarePrediction\\NASADataSet\\'
projectName="PC5.csv"
dataset = np.loadtxt(projectDirPath+projectName, delimiter=",",skiprows=1)
length=len(dataset[0])
x = dataset[:,0:length-1]
y = dataset[:,length-1]
y[y>=1]=1
model = ExtraTreesClassifier()
model.fit(x, y)
# display the relative importance of each attribute
f=open(projectDirPath+projectName)
metrics=f.readline().split(",")
result=model.feature_importances_
MetricsNameList=[]
for i in range(length-1):
    MetricsNameList.append(MetricsName(metrics[i],result[i]))
print(model.feature_importances_)
sorted_result = sorted(MetricsNameList, key=lambda metrics: -metrics.__value__)
result=""
resultNumber=""
for value in sorted_result:
    result+=value.__name__+","
    resultNumber+=str(value.__value__)+","
    print(value.__name__+","+str(value.__value__))
print(result)
#print(resultNumber)