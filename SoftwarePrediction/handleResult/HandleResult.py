import random

from handleResult.ResultStruct import ResultStruct

project='xerces-1.3'
analysisFile=project+'.csvKNN.IMAHAKIL.txt'
with open(analysisFile,'r') as f1:
    listFile=f1.readlines()

resultList=[]
doc = open(project+".result", 'a')
doc.write("##########################"+analysisFile+"##########################\n")
for i in range(0,len(listFile)):
    listFile[i]=listFile[i].strip('\n')

for i in range(0,len(listFile)):
    if listFile[i].startswith('acc'):
        #(self,accList,precisionList,recallList,f1List,gMeanList,matrixList,avgAcc,avgPrecision,avgRecall,avgF1,avgGmean)
        accList=[];precisionList=[];recallList=[];f1List=[];gMeanList=[];matrixList=[]
        accList=listFile[i].split(":")[1].split(",")
        i=i+1
        precisionList=listFile[i].split(":")[1].split(",")
        i=i+1
        recallList=listFile[i].split(":")[1].split(",")
        i=i+1
        f1List=listFile[i].split(":")[1].split(",")
        i=i+1
        gMeanList=listFile[i].split(":")[1].split(",")
        i=i+1
        matrixList=listFile[i].split(":")[1].split(",")
        i=i+1
        tempList=listFile[i].split(":")[1].split(",")
        avgAcc=tempList[0]
        avgPrecision=tempList[1]
        avgRecall=tempList[2]
        avgF1=tempList[3]
        avgGmean=tempList[4]
        resultList.append(ResultStruct(accList,precisionList,recallList,f1List,gMeanList,matrixList,avgAcc,avgPrecision,avgRecall,avgF1,avgGmean))

resultList=sorted(resultList, key=lambda Gmean: Gmean.getAvgGeam(),reverse=True)

randomList=random.sample(range(0,20),5)
#randomList=[2,3,4,5,6]
totalF1=0
totalGmean=0
for i in randomList:
    totalF1+=float(resultList[i].getAvgF1())
    totalGmean+=float(resultList[i].getAvgGeam())
print("F1:"+str(totalF1/5)+" "+"Gmean:"+str(totalGmean/5))


for index in randomList:
    doc.write("-----------------------\n")
    doc.write("acc:")
    for i in range(len(accList) - 1):
        doc.write(str(resultList[index].get_accList()[i]) + ",")
    doc.write(str(resultList[index].get_accList()[len(accList) - 1]))
    doc.write("\n")

    doc.write("precision:")
    for i in range(len(precisionList) - 1):
        doc.write(str(resultList[index].get_precisionList()[i]) + ",")
    doc.write(str(resultList[index].get_precisionList()[len(precisionList) - 1]))
    doc.write("\n")

    doc.write("recall:")
    for i in range(len(recallList) - 1):
        doc.write(str(resultList[index].get_recallList()[i]) + ",")
    doc.write(str(resultList[index].get_recallList()[len(recallList) - 1]))
    doc.write("\n")

    doc.write("f1:")
    for i in range(len(f1List) - 1):
        doc.write(str(resultList[index].getF1List()[i]) + ",")
    doc.write(str(resultList[index].getF1List()[len(f1List) - 1]))
    doc.write("\n")

    doc.write("gMean:")
    for i in range(len(gMeanList) - 1):
        doc.write(str(resultList[index].getGmeanList()[i]) + ",")
    doc.write(str(resultList[index].getGmeanList()[len(gMeanList) - 1]))
    doc.write("\n")

    doc.write("matrix:")
    for i in range(len(matrixList) - 1):
        doc.write(str(resultList[index].getMatrixList()[i]) + ",")
    doc.write(str(resultList[index].getMatrixList()[len(matrixList) - 1]))
    doc.write("\n")

    doc.write("avg:" + str(resultList[index].getAvgAcc()) + "," + str(resultList[index].getAvgPrecision()) + "," + str(resultList[index].getAvgRecall()) + "," + str(resultList[index].getAvgF1()) + "," + str(resultList[index].getAvgGeam()) + "\n")
doc.write("*********************************\n")
doc.write("result:"+"F1:"+str(totalF1/5)+" "+"Gmean:"+str(totalGmean/5)+"\n")
doc.write("*********************************\n")
doc.write('\n')
doc.write('\n')
doc.write('\n')