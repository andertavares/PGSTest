#!/usr/bin/python3

import os
import glob
import argparse
from collections import defaultdict


def parse_file(path):
    """
    Parses a result file and returns the count of victories of each player.
    :param path:
    :return: list with two numbers: the victories of each player
    """
    victory_count = [0, 0]  # victories for p1 and p2

    lines = open(path).readlines()[1:]

    for line in lines:
        winner = int(line.split(',')[0])
        if winner == 0:
            victory_count[0] += 1
        elif winner == 1:
            victory_count[0] += 1
    return victory_count


def analyse(inputdir):
    """
    Parses all result files from a directory, returning a dict.
    When used like: returned_dict[mapname][p1name][p2name], it gives
    the number of victores of p1 vs p2 in the given map
    :param inputdir:
    :return:
    """
    file_list = glob.glob(os.path.join(inputdir, '*.csv'))

    # usage: victories[mapname][player1][player2]
    victories = defaultdict(lambda: defaultdict(lambda: defaultdict(lambda: 0)))

    for path in file_list:
        filename = os.path.splitext(os.path.basename(path))[0]
        (p1, p2, mapname) = filename.split('_')

        result = parse_file(path)
        victories[mapname][p1][p2] += result[0]
        victories[mapname][p2][p1] += result[1]

    return victories


def write_crosstable(results, outdir):
    """
    Receives the results, a 3d-dict, like results[mapname][p1name][p2name]
    and generates one file per mapname in outdir. The file is a .csv with
    a crosstable: the number of wins of the row player against the col. player
    :param results:
    :param outdir:
    :return:
    """
    for mapname in results:
        f = open(os.path.join(outdir, 'results_%s.csv' % mapname), 'w')

        players = results[mapname]

        # writes the header
        f.write(','.join(['x'] + [player for player in players]) + '\n')

        # writes the cells (one row at a time)
        for p1 in players:
            # f.write('%s,' p1)
            f.write(','.join([p1] + [str(results[mapname][p1][p2]) for p2 in players]) + '\n')

        f.close()


if __name__ == '__main__':

    parser = argparse.ArgumentParser(
        description='Analyse the output of microRTS matches in the '
                    'PGSTest project and generates the crosstables'
    )

    parser.add_argument(
        'inputdir', help='Directory with the result files'
    )

    parser.add_argument(
        'outdir', help='Directory to generate the crosstables'
    )

    args = parser.parse_args()

    data = analyse(args.inputdir)
    write_crosstable(data, args.outdir)

    print('DONE.')
