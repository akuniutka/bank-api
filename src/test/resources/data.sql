INSERT INTO ACCOUNT (ID, BALANCE)
VALUES
    (1053, 10),
    (1054, 0),
    (1055, 0),
    (1056, 0),
    (1057, 0),
    (1058, 0),
    (1059, 0),
    (1060, 11),
    (1061, 1),
    (1062, 10.01),
    (1063, 10),
    (1064, 0),
    (1065, 10),
    (1066, 10),
    (1067, 10),
    (1068, 10),
    (1069, 10),
    (1070, 10),
    (1071, 0),
    (1072, 0),
    (1073, 0),
    (1074, 10),
    (1075, 0),
    (1076, 10),
    (1077, 0),
    (1078, 0),
    (1079, 0),
    (1080, 0),
    (1081, 0),
    (1082, 0),
    (1083, 10),
    (1084, 0),
    (1085, 1),
    (1086, 0),
    (1087, 20),
    (1088, 0),
    (1089, 10),
    (1090, 0),
    (1091, 10.01),
    (1092, 0),
    (1093, 0);

INSERT INTO OPERATION (ID, ACCOUNT_ID, TYPE, AMOUNT, DATE)
VALUES
    (1, 1069, 'D', 10, '2023-01-01'),
    (2, 1069, 'D', 10, '2023-02-01'),
    (3, 1069, 'W', 1, '2023-03-01'),
    (4, 1069, 'W', 1, '2023-04-01'),
    (5, 1069, 'W', 1, '2023-05-01'),
    (6, 1069, 'W', 1, '2023-06-01'),
    (7, 1069, 'W', 1, '2023-07-01'),
    (8, 1069, 'W', 1, '2023-08-01'),
    (9, 1069, 'W', 1, '2023-09-01'),
    (10, 1069, 'W', 1, '2023-10-01'),
    (11, 1069, 'W', 1, '2023-11-01'),
    (12, 1069, 'W', 1, '2023-12-01'),
    (13, 1070, 'D', 10, '2023-01-01'),
    (14, 1070, 'D', 10, '2023-12-01'),
    (15, 1070, 'W', 1, '2023-02-01'),
    (16, 1070, 'W', 1, '2023-03-01'),
    (17, 1070, 'W', 1, '2023-04-01'),
    (18, 1070, 'W', 1, '2023-05-01'),
    (19, 1070, 'W', 1, '2023-06-01'),
    (20, 1070, 'W', 1, '2023-07-01'),
    (21, 1070, 'W', 1, '2023-08-01'),
    (22, 1070, 'W', 1, '2023-09-01'),
    (23, 1070, 'W', 1, '2023-10-01'),
    (24, 1070, 'W', 1, '2023-11-01'),
    (25, 1071, 'D', 1, '2022-01-01'),
    (26, 1071, 'P', 1, '2022-02-01'),
    (27, 1072, 'R', 1, '2022-02-01'),
    (28, 1072, 'P', 1, '2022-03-01'),
    (29, 1071, 'R', 1, '2022-03-01'),
    (30, 1071, 'P', 1, '2022-04-01'),
    (31, 1072, 'R', 1, '2022-04-01');

INSERT INTO TRANSFER (ID, OUTGOING_TRANSFER_ID, INCOMING_TRANSFER_ID)
VALUES
    (501, 26, 27),
    (502, 28, 29);