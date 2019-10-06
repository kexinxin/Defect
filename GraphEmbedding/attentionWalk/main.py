from attentionwalk import AttentionWalkTrainer
from attentionWalk import parser
from utils import read_graph, tab_printer

def main():
    """
    Parsing command lines, creating target matrix, fitting an Attention Walker and saving the embedding.
    """
    args = parser.parameter_parser()
    tab_printer(args)
    model = AttentionWalkTrainer(args)
    model.fit()
    model.save_model()

if __name__ =="__main__":
    main()
