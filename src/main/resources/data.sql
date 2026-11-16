-- Datos iniciales. Cada INSERT comprueba que no exista, así se puede reiniciar la app sin duplicar.
INSERT INTO categoria (nombre) SELECT 'Ropa' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Ropa');
INSERT INTO categoria (nombre) SELECT 'Calzado' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Calzado');
INSERT INTO categoria (nombre) SELECT 'Accesorios' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Accesorios');
INSERT INTO categoria (nombre) SELECT 'Alimentos' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Alimentos');

INSERT INTO usuario (nombre, email) SELECT 'Usuario Demo', 'demo@techlab.com' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'demo@techlab.com');
