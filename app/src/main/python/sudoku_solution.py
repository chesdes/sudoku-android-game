""" Code by ChatGPT """

import random

def is_safe(board, index, num):
    row, col = index // 9, index % 9
    if num in board[row * 9 : (row + 1) * 9]:
        return False
    if num in [board[col + i * 9] for i in range(9)]:
        return False
    start = (row // 3) * 27 + (col // 3) * 3
    for i in range(3):
        for j in range(3):
            if board[start + i * 9 + j] == num:
                return False
    return True

def remove_numbers(board, cells_to_remove=40):
    board = board[:]
    coordinates = list(range(81))
    random.shuffle(coordinates)

    for index in coordinates:
        if cells_to_remove <= 0:
            break

        backup = board[index]
        board[index] = 0

        if is_safe(board, index, backup):
            cells_to_remove -= 1
    return board