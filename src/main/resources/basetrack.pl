% DATABASE: BASE TRACK

% E ---> External
% I ---> Internal


%straight(id, x0_E, y0_E, x1_E, y1_E, x0_I, y0_I, x1_I, y1_I)
%straight(id(ID), startPointE(X0_E, Y0_E), endPointE(X1_E, Y1_E), startPointI(X0_I, Y0_I), endPointI(X1_I, Y1_I))
straight(id(1), startPointE(272, 170), endPointE(634, 170), startPointI(272, 226), endPointI(634, 226)).
straight(id(3), startPointE(272, 396), endPointE(634, 396), startPointI(272, 340), endPointI(634, 340)).

%Direction ---> -1 or 1
%SP ---> Start Point
%EP ---> End Point

%turn(id, x_center, y_center, x_SP_E, y_SP_E, x_SP_I, y_SP_I, x_EP_E, y_EP_E, x_EP_I, y_EP_I,direction)
%Query: turn(id(ID), center(X, Y), startPointE(X0_E, Y0_E), startPointI(X0_I, YO_I), endPointE(X1_E, Y1_E), endPointI(X1_I, Y1_I), direction(D))
turn(id(2), center(634, 283), startPointE(634, 170), startPointI(634, 226), endPointE(634, 396), endPointI(634, 340), direction(1)).
turn(id(4), center(272, 283), startPointE(272, 170), startPointI(272, 226), endPointE(272, 396), endPointI(272, 340), direction(-1)).