# MAHAKIL
# phase 1 : Diversity Measurement
# phase 2 : Data Partitioning and Pairing
# phase 3 : Synthetic Sample Generation
import numpy as np
from decimal import Decimal
from Distance import Distance


class MAHAKIL(object):
    def __init__(self, pfp=0.3):
        self.data_defect = None  # 保存初始时的缺陷样本
        self.pfp = pfp  # 预期缺陷样本占比
        self.T = 0  # 需要生成的缺陷样本数
        self.new = []  # 存放新生成的样本
        self.data=None
        self.label=None
        self.defect_data=[]


    def calculate_degree(self):
        pos, neg = 0, 0
        for i in range(0, len(self.label)):
            if self.label[i] == 1:
                pos += 1
            elif self.label[i] == 0:
                neg += 1
        ml = max(pos, neg)
        ms = min(pos, neg)
        if pos > neg:
            minority = 0
        else:
            minority = 1
        return ms, ml, minority


    # return : data_new, label_new
    def fit_sample(self, data, label):
        # data : 包含度量信息的样本 数组
        # label : 样本的标签 数组
        data_defect, data_no_defect, label_defect, label_no_defect = [], [], [], []
        # 按照是否有缺陷划分数据集
        for i in range(label.shape[0]):
            if label[i] == 1:
                data_defect.append(data[i])
                self.defect_data.append(data[i])
                label_defect.append(label[i])
            if label[i] == 0:
                data_no_defect.append(data[i])
                label_no_defect.append(label[i])
        #self.T = len(data_no_defect) / (1 - self.pfp) - len(data_no_defect)

        self.data_defect = np.array(data_defect)
        self.data=data
        self.label=label

        self.ms, self.ml, self.minority = self.calculate_degree()
        self.T = (self.ml - self.ms) * self.pfp

        #temp=(len(data_no_defect)-len(data_defect))*self.pfp
        # 计算得到缺陷样本与u的马氏距离
        d = self.mahalanobis_distance(self.data_defect)
        # 降序排序
        d.sort(key=lambda x: x[1], reverse=True)
        # 将初始缺陷数据集一分为二
        k = len(d)
        d_index = [d[i][0] for i in range(k)]
        data_t_sorted = [data_defect[i] for i in d_index]
        mid = int(k/2)
        bin1 = [data_t_sorted[i] for i in range(0, mid)]
        bin2 = [data_t_sorted[i] for i in range(mid, k)]

        doc = open("test.txt", 'w')
        size=bin1[0].shape[0]
        for x in bin1:
            for i in range(size):
                doc.write('{0:.2f}'.format(x[i]).rstrip('0').rstrip('.')+",")
            doc.write("1"+"\n")
        for x in bin2:
            for i in range(size):
                doc.write('{0:.2f}'.format(x[i]).rstrip('0').rstrip('.')+",")
            doc.write("2"+"\n")
        doc.close()

        # 循环迭代生成新样本
        l_ = len(bin1)
        mark = [1, 3, 7, 15, 31, 63,127,255,511,1023]
        p = self.T / l_
        is_full = True
        g = mark.index([m for m in mark if m > p][0]) + 1
        cluster = 2 ** (g - 1)  # 最后一代的子代个数
        if (self.T - mark[g-2]*l_) < cluster:
            # 说明多增加一代，还不如保持少几个的状态（就算每一个集合只有一个元素，也无法填满所有的集合）
            is_full = False
            g -= 1
            k = 0
        else:
            k = l_ - round((self.T - mark[g-2]*l_)/cluster)
        self.generate_new_sample(bin1, bin2, g, l_, k, is_full)
        # 返回数据与标签
        label_new = np.ones(len(self.new))
        return np.append(data, self.new, axis=0), np.append(label, label_new, axis=0)

    def mahalanobis_distance(self, x):
        # x : 数组
        mu = np.mean(x, axis=0)  # 均值
        d = []
        for i in range(x.shape[0]):
            x_mu = np.atleast_2d(x[i] - mu)
            s = self.cov(x)
            d_squre = np.dot(np.dot(x_mu, np.linalg.inv(s)), np.transpose(x_mu))[0][0]
            d_tuple = (i, d_squre)
            d.append(d_tuple)
        return d

    @staticmethod
    def cov(x):
        # x : 数组
        s = np.zeros((x.shape[1], x.shape[1]))
        mu = np.mean(x, axis=0)  # 均值
        for i in range(x.shape[0]):
            x_xbr = np.atleast_2d(x - mu)
            s_i = np.dot(np.transpose(x_xbr), x_xbr)
            s = s + s_i
        return np.divide(s, x.shape[0])


    def generate_rate(self,generateData):
        rate=0
        k=15
        distanceNear=[]
        for i in range(len(self.data)):
            distance=np.sqrt(np.sum(np.square(self.data[i] - generateData)))
            distanceNear.append(Distance(distance,self.label[i]))
        distanceNear = sorted(distanceNear, key=lambda distance: distance.get_distance())
        defectCount=0
        for i in range(k):
            if distanceNear[i].get_label() == 1:
                defectCount=defectCount+1
        return defectCount/k

   # 生成新样本
    def generate_new_sample(self, bin1, bin2, g, l, k, is_full):
        # bin1, bin2 是数组
        # g 遗传的剩余代数
        # l bin1的item数目
        # k 最后一代每个节点需要裁剪的个数
        # is_full 是否溢出，也即最后一代算完，是超出了T，还是未满T
        assert len(bin1) <= len(bin2)
        if g >= 2 or (g == 1 and is_full is False):
            lv_0 = []  # 子代
            for i in range(l):
                # 生成子代
                lv_0.append(np.mean(np.append(np.atleast_2d(bin1[i]), np.atleast_2d(bin2[i]), axis=0), axis=0))
            self.new.extend(lv_0)
            self.generate_new_sample(lv_0, bin1, g - 1, l, k, is_full)
            self.generate_new_sample(lv_0, bin2, g - 1, l, k, is_full)
        if g == 1 and is_full:
            lv_0 = []  # 子代
            for i in range(l):
                # 生成子代
                lv_0.append(np.mean(np.append(np.atleast_2d(bin1[i]), np.atleast_2d(bin2[i]), axis=0), axis=0))
            del lv_0[-1: (-k - 1): -1]
            self.new.extend(lv_0)
