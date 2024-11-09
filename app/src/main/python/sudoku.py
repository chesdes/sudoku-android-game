""" Imports """
from random import shuffle

""" The check() function checks whether a given number can be placed in the next cell. """
def check(num, row, sudoku):
    bool_arr = []

    for k in range(1, row+1):
        bool_arr.append((sudoku[len(sudoku)-(9*k)] != num))
    """ ---------------------------------------------------
        Check if there is the same number in the column.
    """
    
    index = len(sudoku)
    if row not in [3,6]: 
        if (index - row*9) in [0,3,6]:
            if (row+1) % 3 == 0: bool_arr.append(num not in (sudoku[index-9:index-6]+sudoku[index-18:index-15]))
            else: bool_arr.append(num not in sudoku[index-9:index-6])
        elif (index - row*9) in [1,4,7]:
            if (row+1) % 3 == 0: bool_arr.append(num not in (sudoku[index-10:index-7]+sudoku[index-19:index-16]))
            else: bool_arr.append(num not in sudoku[index-10:index-7])
        else:
            if (row+1) % 3 == 0: bool_arr.append(num not in (sudoku[index-11:index-8]+sudoku[index-20:index-16]))
            else: bool_arr.append(num not in sudoku[index-11:index-8])
    """ ---------------------------------------------------------------------------------------------------------
        Check if there is the same number in the same square.
        We check only the upper ones, since the rows below have not yet been generated.
    """        
    
    return all(bool_arr)

""" Sudoku creation function """
def create_sudoku():
    sudoku = list(range(1,10)) 
    shuffle(sudoku)
    """ ----------------------
        Creates and mixes the first row.
    """
    row = 0
    global_mistakes = 0 
    """ ---------------
        Sometimes the Sudoku creation algorithm comes to a dead end, 
        in which case you will need to start from the beginning. 
        The global_mistakes variable tracks the number of failures when selecting numbers, 
        if the bar for possible failures is exceeded, the function will recurse.
    """
    for _ in range(8):
        row += 1
        row_arr = [] # First, form a string in a separate array
        arr = list(range(1,10)) # creating an array with numbers from 1 to 9
        shuffle(arr) # mix this
        while len(arr) != 0:
            index = 0
            mistakes = 0
            """ --------
                The mistakes variable is necessary to count the number of failures 
                when selecting a number, but, unlike global_mistakes, it is limited to one line. 
                If the magnitude of the failures is equal to the length of the array with numbers (arr), 
                the array is generated anew (since in this case it is impossible to find cells for the remaining digits).
            """
            while True:
                index += 1
                if index > len(arr)-1:
                    index = 0
                
                if check(arr[index], row, sudoku + row_arr): # Checking if it is possible to put a number in this cell
                    mistakes = 0
                    row_arr.append(arr[index]) # put number in row 
                    arr.remove(arr[index])
                    break
                elif mistakes == len(arr):
                    """ ------------------
                        The number of failures is equal to the length of the array with numbers, we reset the row and generate it again.
                    """
                    mistakes = 0
                    row_arr = []
                    arr = list(range(1,10))
                    shuffle(arr)
                    break
                else:
                    global_mistakes += 1
                    if global_mistakes == 2000:
                        return create_sudoku()
                    """ ---------------------- 
                        The value of global_mistakes has exceeded the limit of 2000 iterations, we are starting the function again.
                    """
                    mistakes += 1
        sudoku += row_arr
        """ ------------- 
            The row is assembled successfully, we insert it into the Sudoku.
        """
    return sudoku

""" Sudoku output function """
def print_sudoku(sudoku):
    step = -1
    for cell in sudoku:
        step += 1
        if step == 9:
            step = 0
            print("\n", end="")
        print(f" {cell} ", end="")
    print()