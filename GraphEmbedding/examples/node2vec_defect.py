import numpy
import numpy as np
from sklearn import linear_model
from sklearn.metrics import accuracy_score, recall_score, f1_score
from sklearn.model_selection import train_test_split

from ge.classify import read_node_label,Classifier

from ge import Node2Vec

from sklearn.linear_model import LogisticRegression



import matplotlib.pyplot as plt

import networkx as nx

from sklearn.manifold import TSNE



def evaluate_embeddings(embeddings):

    X, Y = read_node_label('../data/flight/igraph2Label.txt',skip_head=True)

    # tr_frac = 0.8
    #
    # print("Training classifier using {:.2f}% nodes...".format(
    #
    #     tr_frac * 100))
    #
    # # clf = Classifier(embeddings=embeddings, clf=LogisticRegression())
    # #
    # # clf.split_train_evaluate(X, Y, tr_frac)
    # X_ = numpy.asarray([embeddings[x] for x in X])
    # print("kexinxin")
    X = numpy.asarray([embeddings[x] for x in X])
    x_train, x_test, y_train, y_test = train_test_split(X, Y, test_size=0.4)
    clf = linear_model.LogisticRegression(solver='liblinear')
    # clf = RandomForestClassifier()
    # clf=tree.DecisionTreeClassifier(criterion='entropy')
    #clf = KNeighborsClassifier()

    clf.fit(x_train, y_train)

    y_pred = clf.predict(x_test)
    # count=0
    # # for i in range(len(result)):
    # #     if result[i]==y_test[i]:
    # #         count=count+1
    # # print(count/len(result))
    y_test=np.array(y_test)
    y_test=y_test.astype(np.int16)
    y_pred=y_pred.astype(np.int16)
    #y_pred.reshape(-1,1)
    #y_test.reshape(-1,1)
    #y_pred=y_pred.tolist()

    #arr=np.array([1,2,3,4])
    print("acc", accuracy_score(y_test, y_pred))
    #print("precision", precision_score(y_test, y_pred))
    print("recall", recall_score(y_test, y_pred))
    print("f1", f1_score(y_test, y_pred))





def plot_embeddings(embeddings,):

    X, Y = read_node_label('../data/flight/igraph2Label.txt',skip_head=True)



    emb_list = []

    for k in X:

        emb_list.append(embeddings[k])

    emb_list = np.array(emb_list)



    model = TSNE(n_components=2)

    node_pos = model.fit_transform(emb_list)



    color_idx = {}

    for i in range(len(X)):

        color_idx.setdefault(Y[i][0], [])

        color_idx[Y[i][0]].append(i)



    for c, idx in color_idx.items():

        plt.scatter(node_pos[idx, 0], node_pos[idx, 1], label=c)  # c=node_colors)

    plt.legend()

    plt.show()

if __name__ == "__main__":
    G = nx.read_edgelist('../data/flight/igraph2Relation.edgelist', create_using=nx.DiGraph(), nodetype=None,
                         data=[('weight', int)])

    model = Node2Vec(G, 5, 80, workers=1,p=0.25,q=2 )
    model.train()
    embeddings = model.get_embeddings()

    evaluate_embeddings(embeddings)
    plot_embeddings(embeddings)