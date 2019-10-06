import numpy as np
import os


class DataSetTool:
    # 读取文件，文件中的属性通过","进行分隔,并返回处理好的数据集
    # 特征个数
    # is_normalized 是否数据归一化
    @staticmethod
    def init_data(file_path, metrics_num=20,is_normalized=False):
        data_list, label_list = [], []
        if 1 == 1:
            # 直接读取文件
            data_file = np.loadtxt(file_path, dtype=float, delimiter=',', usecols=range(0, metrics_num))
            label_file = np.loadtxt(file_path, dtype=float, delimiter=',', usecols=metrics_num)
            if is_normalized:
                # 数据归一化
                data_file -= data_file.min()
                data_file /= data_file.max()
                label_file -= label_file.min()
                label_file /= label_file.max()
            # 加入列表
            data_list.append(data_file)
            label_list.append(label_file)
        return data_list, label_list