programme testcond:

var ent a, b, c;		{adresses variables dans la pile d'ex�cution de MAPILE: 0, 1, 2}
debut
	cond 
		a > b : ecrire(a),
		a < b : ecrire(b)
	aut  
		ecrire(c);
	fcond
fin
