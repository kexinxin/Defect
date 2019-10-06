import argparse

def parameter_parser():
    """
    A method to parse up command line parameters. By default it gives an embedding of the Wikipedia Chameleons dataset.
    The default hyperparameters give a good quality representation without grid search.
    Representations are sorted by node ID.
    """

    parser = argparse.ArgumentParser(description = "Run Attention Walk.")

    parser.add_argument("--edge-path",
                        nargs = "?",
                        default = "../input/igraph2RelationChange.csv",
	                help = "Edge list csv.")

    parser.add_argument("--embedding-path",
                        nargs = "?",
                        default = "../output/igraph2Relation_embedding.csv",
	                help = "Target embedding csv.")

    parser.add_argument("--attention-path",
                        nargs = "?",
                        default = "../output/igraph2Relation_attention.csv",
	                help = "Attention vector csv.")

    parser.add_argument("--dimensions",
                        type = int,
                        default = 128,
	                help = "Number of dimensions. Default is 128.")

    parser.add_argument("--epochs",
                        type = int,
                        default = 300,
	                help = "Number of gradient descent iterations. Default is 200.")

    parser.add_argument("--window-size",
                        type = int,
                        default = 8,
	                help = "Skip-gram window size. Default is 5.")

    parser.add_argument("--num-of-walks",
                        type = int,
                        default = 150,
	                help = "Number of random walks. Default is 80.")

    parser.add_argument("--beta",
                        type = float,
                        default = 0.5,
	                help = "Regularization parameter. Default is 0.5.")

    parser.add_argument("--gamma",
                        type = float,
                        default = 0.5,
	                help = "Regularization parameter. Default is 0.5.")

    parser.add_argument("--learning-rate",
                        type = float,
                        default = 0.01,
	                help = "Gradient descent learning rate. Default is 0.01.")
    
    return parser.parse_args()
