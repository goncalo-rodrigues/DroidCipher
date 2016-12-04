def data_copy(d_from, offset_from, size):
    d_to = str()

    if len(d_from) < offset_from + size:
        upper_bound = len(d_from)
    else:
        upper_bound = offset_from + size

    for i in range(offset_from, upper_bound):
        d_to += d_from[i]

    return d_to
