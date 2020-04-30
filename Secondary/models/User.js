var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserSchema = new Schema({
	username: String,
	password: String,
	name: String,
	photo: { type: String, default: '' },
	created: { type: Date, default: Date.now },
});

var User = mongoose.model('user', UserSchema);

module.exports = User;